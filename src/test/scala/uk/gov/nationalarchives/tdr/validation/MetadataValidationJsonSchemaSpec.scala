package uk.gov.nationalarchives.tdr.validation

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.testkit.{ImplicitSender, TestKit}
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaDefinition.{BASE_SCHEMA, CLOSURE_SCHEMA}
import uk.gov.nationalarchives.tdr.validation.schema.MetadataValidationJsonSchema
import uk.gov.nationalarchives.tdr.validation.schema.MetadataValidationJsonSchema.ObjectMetadata

class MetadataValidationJsonSchemaSpec extends TestKit(ActorSystem("MetadataValidationJsonSchemaSpec")) with ImplicitSender with AnyWordSpecLike {
  private val alternativeHeaderKey = Some("tdrFileHeader")

  "MessageValidationJsonSchema" should {
    "write alternative header in error message when alternative header key provided" in {
      val data: Set[ObjectMetadata] = dataBuilder("Language", "Unknown")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data, alternativeHeaderKey)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "Language", "enum")
    }

    "write base schema header in error message when no alternative header key provided" in {
      val data: Set[ObjectMetadata] = dataBuilder("language", "Unknown")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data, None)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "language", "enum")
    }

    "write base schema header in error message when no alternative header for property" in {
      val data: Set[ObjectMetadata] = dataBuilder("file_size", "Unknown")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data, alternativeHeaderKey)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "file_size", "type")
    }
  }

  "MetadataValidationJsonSchema BASE_SCHEMA" should {
    "validate incorrect value in enumerated array" in {
      val data: Set[ObjectMetadata] = dataBuilder("language", "Unknown")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data, None)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "language", "enum")
    }
    "validate correct value enumerated array" in {
      val data: Set[ObjectMetadata] = dataBuilder("language", "Welsh")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data, None)
      validationErrors("file1").size shouldBe 0
    }
    "validate array can be null" in {
      val data: Set[ObjectMetadata] = dataBuilder("language", "")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data, None)
      validationErrors("file1").size shouldBe 0
    }
    "validate incorrect date format" in {
      val data: Set[ObjectMetadata] = dataBuilder("date_last_modified", "12-12-2012")
      val validationErrors: Map[String, List[Error]] = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data, None)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "date_last_modified", "format.date")
    }
    "validate correct date format" in {
      val data: Set[ObjectMetadata] = dataBuilder("date_last_modified", "2023-12-05")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data, None)
      validationErrors("file1").size shouldBe 0
    }
    "validate date last modified must have a value" in {
      val data: Set[ObjectMetadata] = dataBuilder("date_last_modified", "")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data, None)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "date_last_modified", "type")
    }
    "validate end date can be empty" in {
      val data: Set[ObjectMetadata] = dataBuilder("end_date", "")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data, None)
      validationErrors("file1").size shouldBe 0
    }
    "validate end date must be before today" in {
      val data: Set[ObjectMetadata] = dataBuilder("end_date", "3000-12-12")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data, None)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "end_date", "daBeforeToday")
    }
    "validate closure period must be a number" in {
      val data: Set[ObjectMetadata] = dataBuilder("closure_period", "123")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data, None)
      validationErrors("file1").size shouldBe 0
    }
    "validate closure period must be less than 150" in {
      val data: Set[ObjectMetadata] = dataBuilder("closure_period", "151")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data, None)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "closure_period", "maximum")
    }
    "validate closure period must be at least 1" in {
      val data: Set[ObjectMetadata] = dataBuilder("closure_period", "0")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data, None)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "closure_period", "minimum")
    }
    "validate closure period can be 150" in {
      val data: Set[ObjectMetadata] = dataBuilder("closure_period", "150")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data, None)
      validationErrors("file1").size shouldBe 0
    }
    "validate closure period can be 1" in {
      val data: Set[ObjectMetadata] = dataBuilder("closure_period", "1")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data, None)
      validationErrors("file1").size shouldBe 0
    }
    "validate title closed is a boolean" in {
      val data: Set[ObjectMetadata] = dataBuilder("title_closed", "Yes")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data, None)
      validationErrors("file1").size shouldBe 0
    }
    "validate title closed not yes/no" in {
      val data: Set[ObjectMetadata] = dataBuilder("title_closed", "blah")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data, None)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "title_closed", "unionType")
    }
    "validate file path ok with one character" in {
      val data: Set[ObjectMetadata] = dataBuilder("file_path", "b")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data, None)
      validationErrors("file1").size shouldBe 0
    }
    "validate file path must have content" in {
      val data: Set[ObjectMetadata] = dataBuilder("file_path", "")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data, None)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "file_path", "type")
    }
  }

  "MetadataValidationJsonSchema CLOSURE_SCHEMA" should {

    "not return any errors when closure_type is Open" in {
      val data: Set[ObjectMetadata] = Set(ObjectMetadata("file1", Set(Metadata("closure_type", "Open"))))
      val validationErrors = MetadataValidationJsonSchema.validate(CLOSURE_SCHEMA, data, None)
      validationErrors("file1") shouldBe List.empty
    }

    "not return any errors when closure_type is Closed" in {
      val data: Set[ObjectMetadata] = closureDataBuilder("1990-01-12", "33", "12", "1990-11-12", "No", "No")
      val validationErrors = MetadataValidationJsonSchema.validate(CLOSURE_SCHEMA, data, None)
      validationErrors("file1") shouldBe List.empty
    }

    "return errors with all the required fields when closure_type is Closed" in {
      val closure = Metadata("closure_type", "Closed")
      val data: Set[ObjectMetadata] = Set(ObjectMetadata("file1", Set(closure)))
      val validationErrors = MetadataValidationJsonSchema.validate(CLOSURE_SCHEMA, data, None)
      validationErrors("file1").size shouldBe 6
      validationErrors("file1") should contain theSameElementsAs List(
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
      val validationErrors = MetadataValidationJsonSchema.validate(CLOSURE_SCHEMA, data, None)
      validationErrors("file1").size shouldBe 6
      validationErrors("file1") should contain theSameElementsAs List(
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
      val validationErrors = MetadataValidationJsonSchema.validate(CLOSURE_SCHEMA, data, None)
      validationErrors("file1").size shouldBe 6
      validationErrors("file1") should contain theSameElementsAs List(
        Error("closure_start_date", "type"),
        Error("foi_exemption_code", "type"),
        Error("closure_period", "type"),
        Error("foi_exemption_asserted", "type"),
        Error("description_closed", "type"),
        Error("title_closed", "type")
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
          Metadata("closure_type", "Closed"),
          Metadata("closure_start_date", closureStartDate),
          Metadata("foi_exemption_code", foiCodes),
          Metadata("closure_period", closurePeriod),
          Metadata("foi_exemption_asserted", foiDecisionAsserted),
          Metadata("description_closed", descriptionClosed),
          Metadata("title_closed", titleClosed)
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
