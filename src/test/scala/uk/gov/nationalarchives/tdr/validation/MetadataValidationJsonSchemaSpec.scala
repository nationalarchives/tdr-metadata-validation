package uk.gov.nationalarchives.tdr.validation

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.testkit.{ImplicitSender, TestKit}
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaDefinition.BASE_SCHEMA
import uk.gov.nationalarchives.tdr.validation.schema.MetadataValidationJsonSchema
import uk.gov.nationalarchives.tdr.validation.schema.MetadataValidationJsonSchema.ObjectMetadata

class MetadataValidationJsonSchemaSpec extends TestKit(ActorSystem("MySpec")) with ImplicitSender with AnyWordSpecLike {

  "MetadataValidationJsonSchema" should {
    "validate incorrect value enumerated array " in {
      val data: Set[ObjectMetadata] = dataBuilder("Language", "Unknown")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "language", "enum")
    }
  }
  "validate correct value enumerated array " in {
    val data: Set[ObjectMetadata] = dataBuilder("Language", "Welsh")
    val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data)
    validationErrors("file1").size shouldBe 0
  }
  "validate incorrect date " in {
    val data: Set[ObjectMetadata] = dataBuilder("Date last modified", "12-12-2012")
    val validationErrors: Map[String, List[Error]] = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data)
    validationErrors("file1").size shouldBe 1
  }
  "validate correct date" in {
    val data: Set[ObjectMetadata] = dataBuilder("Date last modified", "2023-12-05")
    val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data)
    validationErrors("file1").size shouldBe 0
    singleErrorCheck(validationErrors, "date_last_modified", "format.date")
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
    val data: Set[ObjectMetadata] = dataBuilder("Date of the record", "2044-12-12")
    val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data)
    validationErrors("file1").size shouldBe 1
    singleErrorCheck(validationErrors, "end_date", "daBeforeToday")
  }
  "closure period must be a number" in {
    val data: Set[ObjectMetadata] = dataBuilder("Closure Period", "123")
    val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data)
    validationErrors("file1").size shouldBe 0
  }
  "closure period must be less than 150" in {
    val data: Set[ObjectMetadata] = dataBuilder("Closure Period", "155")
    val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data)
    validationErrors("file1").size shouldBe 1
    singleErrorCheck(validationErrors, "closure_period", "maximum")
  }
  "closure period must be at least 1" in {
    val data: Set[ObjectMetadata] = dataBuilder("Closure Period", "0")
    val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data)
    validationErrors("file1").size shouldBe 1
    singleErrorCheck(validationErrors, "closure_period", "minimum")
  }
  "closure period can be 1" in {
    val data: Set[ObjectMetadata] = dataBuilder("Closure Period", "1")
    val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data)
    validationErrors("file1").size shouldBe 0
  }
  "title closed is a boolean" in {
    val data: Set[ObjectMetadata] = dataBuilder("Is the title sensitive for the public?", "Yes")
    val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data)
    validationErrors("file1").size shouldBe 0
  }
  "title closed not yes/no" in {
    val data: Set[ObjectMetadata] = dataBuilder("Is the title sensitive for the public?", "blah")
    val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data)
    validationErrors("file1").size shouldBe 1
    singleErrorCheck(validationErrors, "title_closed", "unionType")
  }

  private def dataBuilder(key: String, value: String) = {
    val titleClosed = Metadata(key, value)
    Set(ObjectMetadata("file1", Set(titleClosed)))
  }

  private def singleErrorCheck(validationErrors: Map[String, List[Error]], propertyName: String, value: Any): Unit = {
    validationErrors.foreach(x => x._2.foreach(error => {
      error.propertyName shouldBe propertyName
      error.errorCode shouldBe value
    }
    ))
  }
}
