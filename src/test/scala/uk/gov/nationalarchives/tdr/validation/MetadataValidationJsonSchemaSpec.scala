package uk.gov.nationalarchives.tdr.validation

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.testkit.{ImplicitSender, TestKit}
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaDefinition.{BASE_SCHEMA, CLOSURE_SCHEMA, DATA_LOAD_SHAREPOINT_SCHEMA}
import uk.gov.nationalarchives.tdr.validation.schema.{JsonSchemaDefinition, MetadataValidationJsonSchema}
import uk.gov.nationalarchives.tdr.validation.schema.MetadataValidationJsonSchema.ObjectMetadata
import uk.gov.nationalarchives.tdr.validation.utils.CSVtoJsonUtils

class MetadataValidationJsonSchemaSpec extends TestKit(ActorSystem("MetadataValidationJsonSchemaSpec")) with ImplicitSender with AnyWordSpecLike {

  "MetadataValidationJsonSchema multiple schema" should {
    val schemaDefinitions: Set[JsonSchemaDefinition] = Set(BASE_SCHEMA, CLOSURE_SCHEMA)
    "validate data against all schema and return no errors if data valid" in {
      val data: Set[ObjectMetadata] =
        Set(ObjectMetadata("file1", Set(Metadata("closure_type", "Open")))) ++ dataBuilder("Language", "Welsh")
      val validationResults = MetadataValidationJsonSchema.validate(schemaDefinitions, data, Some("tdrFileHeader"))
      validationResults.size shouldBe 2
      validationResults.head.schemaLocation shouldBe BASE_SCHEMA.location
      validationResults.head.schemaErrors.head._2.size shouldBe 0
      validationResults.last.schemaLocation shouldBe CLOSURE_SCHEMA.location
      validationResults.last.schemaErrors.head._2.size shouldBe 0
    }

    "validate data against all schema and return expected errors if data invalid" in {
      val data: Set[ObjectMetadata] =
        Set(
          ObjectMetadata(
            "file1",
            Set(
              Metadata("closure_type", "Closed"),
              Metadata("Language", "Unknown")
            )
          )
        )
      val validationResults = MetadataValidationJsonSchema.validate(Set(BASE_SCHEMA, CLOSURE_SCHEMA), data, Some("tdrFileHeader"))
      validationResults.size shouldBe 2
      validationResults.head.schemaLocation shouldBe BASE_SCHEMA.location
      validationResults.head.schemaErrors.head._2.size shouldBe 1
      validationResults.last.schemaLocation shouldBe CLOSURE_SCHEMA.location
      validationResults.last.schemaErrors.head._2.size shouldBe 6
    }
  }

  "MetadataValidationJsonSchema BASE_SCHEMA" should {
    "validate incorrect value in enumerated array" in {
      val data: Set[ObjectMetadata] = dataBuilder("Language", "Unknown")
      val validationResults = MetadataValidationJsonSchema.validate(Set(BASE_SCHEMA), data)
      validationResults.size shouldBe 1
      validationResults.head.schemaLocation shouldBe BASE_SCHEMA.location
      singleErrorCheck(validationResults.head.schemaErrors, "language", "enum")
    }
    "validate correct value enumerated array" in {
      val data: Set[ObjectMetadata] = dataBuilder("Language", "Welsh")
      val validationResults = MetadataValidationJsonSchema.validate(Set(BASE_SCHEMA), data)
      validationResults.size shouldBe 1
      validationResults.head.schemaLocation shouldBe BASE_SCHEMA.location
      validationResults.head.schemaErrors("file1").size shouldBe 0
    }
    "validate array can be null" in {
      val data: Set[ObjectMetadata] = dataBuilder("Language", "")
      val validationResults = MetadataValidationJsonSchema.validate(Set(BASE_SCHEMA), data)
      validationResults.size shouldBe 1
      validationResults.head.schemaLocation shouldBe BASE_SCHEMA.location
      validationResults.head.schemaErrors("file1").size shouldBe 0
    }
    "validate incorrect date format" in {
      val data: Set[ObjectMetadata] = dataBuilder("Date last modified", "12-12-2012")
      val validationResults = MetadataValidationJsonSchema.validate(Set(BASE_SCHEMA), data, Some("tdrFileHeader"))
      validationResults.size shouldBe 1
      validationResults.head.schemaLocation shouldBe BASE_SCHEMA.location
      validationResults.head.schemaErrors("file1").size shouldBe 1
      singleErrorCheck(validationResults.head.schemaErrors, "date_last_modified", "format.date")
    }
    "validate correct date format" in {
      val data: Set[ObjectMetadata] = dataBuilder("Date last modified", "2023-12-05")
      val validationResults = MetadataValidationJsonSchema.validate(Set(BASE_SCHEMA), data)
      validationResults.size shouldBe 1
      validationResults.head.schemaLocation shouldBe BASE_SCHEMA.location
      validationResults.head.schemaErrors("file1").size shouldBe 0
    }
    "validate date last modified must have a value" in {
      val data: Set[ObjectMetadata] = dataBuilder("Date last modified", "")
      val validationResults = MetadataValidationJsonSchema.validate(Set(BASE_SCHEMA), data, Some("tdrFileHeader"))
      validationResults.size shouldBe 1
      validationResults.head.schemaLocation shouldBe BASE_SCHEMA.location
      validationResults.head.schemaErrors("file1").size shouldBe 1
      singleErrorCheck(validationResults.head.schemaErrors, "date_last_modified", "type")
    }
    "validate end date can be empty" in {
      val data: Set[ObjectMetadata] = dataBuilder("Date of the record", "")
      val validationResults = MetadataValidationJsonSchema.validate(Set(BASE_SCHEMA), data)
      validationResults.size shouldBe 1
      validationResults.head.schemaLocation shouldBe BASE_SCHEMA.location
      validationResults.head.schemaErrors("file1").size shouldBe 0
    }
    "validate end date must be before today" in {
      val data: Set[ObjectMetadata] = dataBuilder("Date of the record", "3000-12-12")
      val validationResults = MetadataValidationJsonSchema.validate(Set(BASE_SCHEMA), data, Some("tdrFileHeader"))
      validationResults.size shouldBe 1
      validationResults.head.schemaLocation shouldBe BASE_SCHEMA.location
      validationResults.head.schemaErrors("file1").size shouldBe 1
      singleErrorCheck(validationResults.head.schemaErrors, "end_date", "daBeforeToday")
    }
    "validate closure period must be a number" in {
      val data: Set[ObjectMetadata] = dataBuilder("Closure Period", "123")
      val validationResults = MetadataValidationJsonSchema.validate(Set(BASE_SCHEMA), data)
      validationResults.size shouldBe 1
      validationResults.head.schemaLocation shouldBe BASE_SCHEMA.location
      validationResults.head.schemaErrors("file1").size shouldBe 0
    }
    "validate closure period must be less than 150" in {
      val data: Set[ObjectMetadata] = dataBuilder("Closure Period", "151")
      val validationResults = MetadataValidationJsonSchema.validate(Set(BASE_SCHEMA), data, Some("tdrFileHeader"))
      validationResults.size shouldBe 1
      validationResults.head.schemaLocation shouldBe BASE_SCHEMA.location
      validationResults.head.schemaErrors("file1").size shouldBe 1
      singleErrorCheck(validationResults.head.schemaErrors, "closure_period", "maximum")
    }
    "validate closure period must be at least 1" in {
      val data: Set[ObjectMetadata] = dataBuilder("Closure Period", "0")
      val validationResults = MetadataValidationJsonSchema.validate(Set(BASE_SCHEMA), data, Some("tdrFileHeader"))
      validationResults.size shouldBe 1
      validationResults.head.schemaLocation shouldBe BASE_SCHEMA.location
      validationResults.head.schemaErrors("file1").size shouldBe 1
      singleErrorCheck(validationResults.head.schemaErrors, "closure_period", "minimum")
    }
    "validate closure period can be 150" in {
      val data: Set[ObjectMetadata] = dataBuilder("Closure Period", "150")
      val validationResults = MetadataValidationJsonSchema.validate(Set(BASE_SCHEMA), data)
      validationResults.size shouldBe 1
      validationResults.head.schemaLocation shouldBe BASE_SCHEMA.location
      validationResults.head.schemaErrors("file1").size shouldBe 0
    }
    "validate closure period can be 1" in {
      val data: Set[ObjectMetadata] = dataBuilder("Closure Period", "1")
      val validationResults = MetadataValidationJsonSchema.validate(Set(BASE_SCHEMA), data)
      validationResults.size shouldBe 1
      validationResults.head.schemaLocation shouldBe BASE_SCHEMA.location
      validationResults.head.schemaErrors("file1").size shouldBe 0
    }
    "validate title closed is a boolean" in {
      val data: Set[ObjectMetadata] = dataBuilder("Is the title sensitive for the public?", "Yes")
      val validationResults = MetadataValidationJsonSchema.validate(Set(BASE_SCHEMA), data)
      validationResults.size shouldBe 1
      validationResults.head.schemaLocation shouldBe BASE_SCHEMA.location
      validationResults.head.schemaErrors("file1").size shouldBe 0
    }
    "validate title closed not yes/no" in {
      val data: Set[ObjectMetadata] = dataBuilder("Is the title sensitive for the public?", "blah")
      val validationResults = MetadataValidationJsonSchema.validate(Set(BASE_SCHEMA), data, Some("tdrFileHeader"))
      validationResults.size shouldBe 1
      validationResults.head.schemaLocation shouldBe BASE_SCHEMA.location
      validationResults.head.schemaErrors("file1").size shouldBe 1
      singleErrorCheck(validationResults.head.schemaErrors, "title_closed", "unionType")
    }
    "validate file path ok with one character" in {
      val data: Set[ObjectMetadata] = dataBuilder("Filepath", "b")
      val validationResults = MetadataValidationJsonSchema.validate(Set(BASE_SCHEMA), data)
      validationResults.size shouldBe 1
      validationResults.head.schemaLocation shouldBe BASE_SCHEMA.location
      validationResults.head.schemaErrors("file1").size shouldBe 0
    }
    "validate file path must have content" in {
      val data: Set[ObjectMetadata] = dataBuilder("Filepath", "")
      val validationResults = MetadataValidationJsonSchema.validate(Set(BASE_SCHEMA), data, Some("tdrFileHeader"))
      validationResults.size shouldBe 1
      validationResults.head.schemaLocation shouldBe BASE_SCHEMA.location
      validationResults.head.schemaErrors("file1").size shouldBe 1
      singleErrorCheck(validationResults.head.schemaErrors, "file_path", "type")
    }
  }

  "MetadataValidationJsonSchema CLOSURE_SCHEMA" should {

    "not return any errors when closure_type is Open" in {
      val data: Set[ObjectMetadata] = Set(ObjectMetadata("file1", Set(Metadata("closure_type", "Open"))))
      val validationResults = MetadataValidationJsonSchema.validate(Set(CLOSURE_SCHEMA), data)
      validationResults.size shouldBe 1
      validationResults.head.schemaLocation shouldBe CLOSURE_SCHEMA.location
      validationResults.head.schemaErrors("file1") shouldBe List.empty
    }

    "not return any errors when closure_type is Closed" in {
      val data: Set[ObjectMetadata] = closureDataBuilder("1990-01-12", "33", "12", "1990-11-12", "No", "No")
      val validationResults = MetadataValidationJsonSchema.validate(Set(CLOSURE_SCHEMA), data)
      validationResults.size shouldBe 1
      validationResults.head.schemaLocation shouldBe CLOSURE_SCHEMA.location
      validationResults.head.schemaErrors("file1") shouldBe List.empty
    }

    "return errors with all the required fields when closure_type is Closed" in {
      val closure = Metadata("closure_type", "Closed")
      val data: Set[ObjectMetadata] = Set(ObjectMetadata("file1", Set(closure)))
      val validationResults = MetadataValidationJsonSchema.validate(Set(CLOSURE_SCHEMA), data)
      validationResults.size shouldBe 1
      validationResults.head.schemaLocation shouldBe CLOSURE_SCHEMA.location
      validationResults.head.schemaErrors("file1").size shouldBe 6
      validationResults.head.schemaErrors("file1") should contain theSameElementsAs List(
        Error("closure_start_date", "required"),
        Error("foi_exemption_code", "required"),
        Error("closure_period", "required"),
        Error("foi_exemption_asserted", "required"),
        Error("description_closed", "required"),
        Error("title_closed", "required")
      )
    }

    "return errors if required fields has invalid values when closure_type is Closed" in {
      val data: Set[ObjectMetadata] = closureDataBuilder("1990--12", "55", "-12", "1990--12", "tttt", "tttt")
      val validationResults = MetadataValidationJsonSchema.validate(Set(CLOSURE_SCHEMA), data, Some("tdrFileHeader"))
      validationResults.size shouldBe 1
      validationResults.head.schemaLocation shouldBe CLOSURE_SCHEMA.location
      validationResults.head.schemaErrors("file1").size shouldBe 6
      validationResults.head.schemaErrors("file1") should contain theSameElementsAs List(
        Error("closure_start_date", "format.date"),
        Error("foi_exemption_code", "enum"),
        Error("closure_period", "minimum"),
        Error("foi_exemption_asserted", "format.date"),
        Error("description_closed", "type"),
        Error("title_closed", "type")
      )
    }

    "return errors if required fields have empty values when closure_type is Closed" in {
      val data: Set[ObjectMetadata] = closureDataBuilder("", "", "", "", "", "")
      val validationResults = MetadataValidationJsonSchema.validate(Set(CLOSURE_SCHEMA), data, Some("tdrFileHeader"))
      validationResults.size shouldBe 1
      validationResults.head.schemaLocation shouldBe CLOSURE_SCHEMA.location
      validationResults.head.schemaErrors("file1").size shouldBe 6
      validationResults.head.schemaErrors("file1") should contain theSameElementsAs List(
        Error("closure_start_date", "type"),
        Error("foi_exemption_code", "type"),
        Error("closure_period", "type"),
        Error("foi_exemption_asserted", "type"),
        Error("description_closed", "type"),
        Error("title_closed", "type")
      )
    }
  }

  "MetadataValidationJsonSchema DATA_LOAD_SHAREPOINT_SCHEMA" should {
    "validate if all required properties are present" in {
      val validMetadata = Set(
        Metadata("ClientSideFileLastModifiedDate", "2001-12-12"),
        Metadata("UUID", "b8b624e4-ec68-4e08-b5db-dfdc9ec84fea"),
        Metadata("ClientSideOriginalFilepath", "a/filepath/filename1.docx"),
        Metadata("SHA256ClientSideChecksum", "8b9118183f01b3df0fc5073feb68f0ecd5a7f85a88ed63ac7d0d242dc2aba2ea"),
        Metadata("ClientSideFileSize", "26")
      )
      val data: Set[ObjectMetadata] = Set(
        ObjectMetadata("file1", validMetadata)
      )
      val validationErrors = MetadataValidationJsonSchema.validate(Set(DATA_LOAD_SHAREPOINT_SCHEMA), data, Some("tdrDataLoadHeader"))
      validationErrors.size shouldBe 1
      validationErrors.head.schemaErrors.size shouldBe 1
      validationErrors.head.schemaErrors.head._2.size shouldBe 0
    }

    "return errors for all missing required properties" in {
      val data: Set[ObjectMetadata] = Set(
        ObjectMetadata("file1", Set())
      )
      val validationErrors = MetadataValidationJsonSchema.validate(Set(DATA_LOAD_SHAREPOINT_SCHEMA), data, Some("tdrDataLoadHeader"))
      validationErrors.size shouldBe 1
      validationErrors.head.schemaErrors.size shouldBe 1
      validationErrors.head.schemaErrors.head._2 should contain theSameElementsAs List(
        Error("UUID", "required"),
        Error("ClientSideFileSize", "required"),
        Error("SHA256ClientSideChecksum", "required"),
        Error("ClientSideFileLastModifiedDate", "required"),
        Error("ClientSideOriginalFilepath", "required")
      )
    }
  }

  "MetadataValidationJsonSchema validate List[FileRow]" should {
    "validate file rows" in {
      val lastModified = Metadata("Date last modified", "12-12-2012")
      val language = Metadata("Language", "Unknown")
      val fileRow1 = FileRow("file_a", List(lastModified, language))
      val fileRow2 = FileRow("file_b", List(lastModified, language))
      val errors = MetadataValidationJsonSchema.validate(List(fileRow1, fileRow2))
      errors.size shouldBe 2
    }
  }

  private def dataBuilder(key: String, value: String): Set[ObjectMetadata] = {
    val metadata = Metadata(key, value)
    Set(ObjectMetadata("file1", Set(metadata)))
  }

  private def closureDataBuilder(
      closureStartDate: String,
      foiCodes: String,
      closurePeriod: String,
      foiDecisionAsserted: String,
      descriptionClosed: String,
      titleClosed: String
  ): Set[ObjectMetadata] = {
    Set(
      ObjectMetadata(
        "file1",
        Set(
          Metadata("Closure status", "Closed"),
          Metadata("Closure Start Date", closureStartDate),
          Metadata("FOI exemption code", foiCodes),
          Metadata("Closure Period", closurePeriod),
          Metadata("FOI decision asserted", foiDecisionAsserted),
          Metadata("Is the description sensitive for the public?", descriptionClosed),
          Metadata("Is the title sensitive for the public?", titleClosed)
        )
      )
    )
  }

  private def singleErrorCheck(validationErrors: Map[String, List[Error]], propertyName: String, value: Any): Unit = {
    validationErrors.foreach(objectIdentifierWithErrors =>
      objectIdentifierWithErrors._2.foreach(error => {
        error.propertyName shouldBe propertyName
        error.errorCode shouldBe value
      })
    )
  }
}
