package uk.gov.nationalarchives.tdr.validation

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.testkit.{ImplicitSender, TestKit}
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaDefinition.BASE_SCHEMA
import uk.gov.nationalarchives.tdr.validation.schema.MetadataValidationJsonSchema
import uk.gov.nationalarchives.tdr.validation.schema.MetadataValidationJsonSchema.ObjectMetadata

class MetadataValidationJsonSchemaSpec extends TestKit(ActorSystem("MetadataValidationJsonSchemaSpec")) with ImplicitSender with AnyWordSpecLike {

  "MetadataValidationJsonSchema" should {
    "validate incorrect value in enumerated array" in {
      val data: Set[ObjectMetadata] = dataBuilder("Language", "Unknown")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "language", "enum")
    }
    "validate correct value enumerated array" in {
      val data: Set[ObjectMetadata] = dataBuilder("Language", "Welsh")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }
    "validate array can be null" in {
      val data: Set[ObjectMetadata] = dataBuilder("Language", "")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }
    "validate incorrect date format" in {
      val data: Set[ObjectMetadata] = dataBuilder("Date last modified", "12-12-2012")
      val validationErrors: Map[String, List[Error]] = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "date_last_modified", "format.date")
    }
    "validate correct date format" in {
      val data: Set[ObjectMetadata] = dataBuilder("Date last modified", "2023-12-05")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }
    "validate date last modified must have a value" in {
      val data: Set[ObjectMetadata] = dataBuilder("Date last modified", "")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "date_last_modified", "type")
    }
    "validate end date can be empty" in {
      val data: Set[ObjectMetadata] = dataBuilder("Date of the record", "")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }
    "validate end date must be before today" in {
      val data: Set[ObjectMetadata] = dataBuilder("Date of the record", "3000-12-12")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "end_date", "daBeforeToday")
    }
    "validate closure period must be a number" in {
      val data: Set[ObjectMetadata] = dataBuilder("Closure Period", "123")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }
    "validate closure period must be less than 150" in {
      val data: Set[ObjectMetadata] = dataBuilder("Closure Period", "151")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "closure_period", "maximum")
    }
    "validate closure period must be at least 1" in {
      val data: Set[ObjectMetadata] = dataBuilder("Closure Period", "0")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "closure_period", "minimum")
    }
    "validate closure period can be 150" in {
      val data: Set[ObjectMetadata] = dataBuilder("Closure Period", "150")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }
    "validate closure period can be 1" in {
      val data: Set[ObjectMetadata] = dataBuilder("Closure Period", "1")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }
    "validate title closed is a boolean" in {
      val data: Set[ObjectMetadata] = dataBuilder("Is the title sensitive for the public?", "Yes")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }
    "validate title closed not yes/no" in {
      val data: Set[ObjectMetadata] = dataBuilder("Is the title sensitive for the public?", "blah")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "title_closed", "unionType")
    }
    "validate file path ok with one character" in {
      val data: Set[ObjectMetadata] = dataBuilder("Filepath", "b")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }
    "validate file path must have content" in {
      val data: Set[ObjectMetadata] = dataBuilder("Filepath", "")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "file_path", "type")
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

  private def dataBuilder(key: String, value: String) = {
    val titleClosed = Metadata(key, value)
    Set(ObjectMetadata("file1", Set(titleClosed)))
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
