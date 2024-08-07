package uk.gov.nationalarchives.tdr.validation

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.testkit.{ImplicitSender, TestKit}
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaDefinition.{BASE_SCHEMA, CLOSURE_SCHEMA}
import uk.gov.nationalarchives.tdr.validation.schema.MetadataValidationJsonSchema.ObjectMetadata
import uk.gov.nationalarchives.tdr.validation.schema.ValidationProcess.{SCHEMA_BASE, _}
import uk.gov.nationalarchives.tdr.validation.schema.{MetadataValidationJsonSchema, ValidationError}

class MetadataValidationJsonSchemaSpec extends TestKit(ActorSystem("MetadataValidationJsonSchemaSpec")) with ImplicitSender with AnyWordSpecLike {

  "MetadataValidationJsonSchema with BASE_SCHEMA" should {
    "validate incorrect value in enumerated array" in {
      val data: Set[ObjectMetadata] = dataBuilder("Language", "Unknown")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "language", "enum", SCHEMA_BASE)
    }
    "validate correct value enumerated array" in {
      val data: Set[ObjectMetadata] = dataBuilder("Language", "Welsh")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }
    "validate array can be null" in {
      val data: Set[ObjectMetadata] = dataBuilder("Language", "")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }
    "validate incorrect date format" in {
      val data: Set[ObjectMetadata] = dataBuilder("Date last modified", "12-12-2012")
      val validationErrors: Map[String, List[ValidationError]] = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "date_last_modified", "format.date", SCHEMA_BASE)
    }
    "validate correct date format" in {
      val data: Set[ObjectMetadata] = dataBuilder("Date last modified", "2023-12-05")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }
    "validate date last modified must have a value" in {
      val data: Set[ObjectMetadata] = dataBuilder("Date last modified", "")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "date_last_modified", "type", SCHEMA_BASE)
    }
    "validate end date can be empty" in {
      val data: Set[ObjectMetadata] = dataBuilder("Date of the record", "")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }
    "validate end date must be before today" in {
      val data: Set[ObjectMetadata] = dataBuilder("Date of the record", "3000-12-12")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "end_date", "daBeforeToday", SCHEMA_BASE)
    }
    "validate closure period must be a number" in {
      val data: Set[ObjectMetadata] = dataBuilder("Closure Period", "123")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }
    "validate closure period must be less than 150" in {
      val data: Set[ObjectMetadata] = dataBuilder("Closure Period", "151")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "closure_period", "maximum", SCHEMA_BASE)
    }
    "validate closure period must be at least 1" in {
      val data: Set[ObjectMetadata] = dataBuilder("Closure Period", "0")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "closure_period", "minimum", SCHEMA_BASE)
    }
    "validate closure period can be 150" in {
      val data: Set[ObjectMetadata] = dataBuilder("Closure Period", "150")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }
    "validate closure period can be 1" in {
      val data: Set[ObjectMetadata] = dataBuilder("Closure Period", "1")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }
    "validate title closed is a boolean" in {
      val data: Set[ObjectMetadata] = dataBuilder("Is the title sensitive for the public?", "Yes")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }
    "validate title closed not yes/no" in {
      val data: Set[ObjectMetadata] = dataBuilder("Is the title sensitive for the public?", "blah")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "title_closed", "unionType", SCHEMA_BASE)
    }
    "validate file path ok with one character" in {
      val data: Set[ObjectMetadata] = dataBuilder("Filepath", "b")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }
    "validate file path must have content" in {
      val data: Set[ObjectMetadata] = dataBuilder("Filepath", "")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "file_path", "type", SCHEMA_BASE)
    }
  }

  "MetadataValidationJsonSchema with CLOSURE_SCHEMA" should {

    "not return any errors from Closure Schema when closure_type is Open and start date empty" in {
      val data: Set[ObjectMetadata] = Set(
        ObjectMetadata(
          "file1",
          Set(
            Metadata("closure_type", "Open"),
            Metadata("closure_start_date", "")
          )
        )
      )
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(CLOSURE_SCHEMA, data)
      validationErrors("file1") shouldBe List.empty
    }
    "return any errors from Closure Schema when closure_type is Open and start date is not empty" in {
      val data: Set[ObjectMetadata] = Set(
        ObjectMetadata(
          "file1",
          Set(
            Metadata("closure_type", "Open"),
            Metadata("closure_start_date", "2012-12-12")
          )
        )
      )
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(CLOSURE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "closure_start_date", "maxLength", SCHEMA_CLOSURE)
    }

    "will return any error from closure schema closure_start_date has a value and no closure_type set" in {
      val data: Set[ObjectMetadata] = Set(ObjectMetadata("file1", Set(Metadata("closure_start_date", "some data"))))
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(CLOSURE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "closure_start_date", "maxLength", SCHEMA_CLOSURE)
    }

    "not return any errors when closure_type is Closed" in {
      val data: Set[ObjectMetadata] = closureDataBuilder("1990-01-12", "33", "12", "1990-11-12", "No", "No")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(CLOSURE_SCHEMA, data)
      validationErrors("file1") shouldBe List.empty
    }

    "return errors with all the required fields when closure_type is Closed" in {
      val closure = Metadata("closure_type", "Closed")
      val data: Set[ObjectMetadata] = Set(ObjectMetadata("file1", Set(closure)))
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(CLOSURE_SCHEMA, data)
      validationErrors("file1").size shouldBe 6
      validationErrors("file1") should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE, "closure_start_date", "required"),
        ValidationError(SCHEMA_CLOSURE, "foi_exemption_code", "required"),
        ValidationError(SCHEMA_CLOSURE, "closure_period", "required"),
        ValidationError(SCHEMA_CLOSURE, "foi_exemption_asserted", "required"),
        ValidationError(SCHEMA_CLOSURE, "description_closed", "required"),
        ValidationError(SCHEMA_CLOSURE, "title_closed", "required")
      )
    }

    "return errors if required fields has invalid values when closure_type is Closed" in {
      val data: Set[ObjectMetadata] = closureDataBuilder("1990--12", "55", "-12", "1990--12", "tttt", "tttt")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(CLOSURE_SCHEMA, data)
      validationErrors("file1").size shouldBe 6
      validationErrors("file1") should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE, "closure_start_date", "format.date"),
        ValidationError(SCHEMA_CLOSURE, "foi_exemption_code", "enum"),
        ValidationError(SCHEMA_CLOSURE, "closure_period", "minimum"),
        ValidationError(SCHEMA_CLOSURE, "foi_exemption_asserted", "format.date"),
        ValidationError(SCHEMA_CLOSURE, "description_closed", "type"),
        ValidationError(SCHEMA_CLOSURE, "title_closed", "type")
      )
    }

    "return errors if required fields have empty values when closure_type is Closed" in {
      val data: Set[ObjectMetadata] = closureDataBuilder("", "", "", "", "", "")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(CLOSURE_SCHEMA, data)
      validationErrors("file1").size shouldBe 6
      validationErrors("file1") should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE, "closure_start_date", "type"),
        ValidationError(SCHEMA_CLOSURE, "foi_exemption_code", "type"),
        ValidationError(SCHEMA_CLOSURE, "closure_period", "type"),
        ValidationError(SCHEMA_CLOSURE, "foi_exemption_asserted", "type"),
        ValidationError(SCHEMA_CLOSURE, "description_closed", "type"),
        ValidationError(SCHEMA_CLOSURE, "title_closed", "type")
      )
    }
  }

  "MetadataValidationJsonSchema validate with both BASE_SCHEMA and CLOSURE_SCHEMA" should {
    "validate file rows that only have base schema errors" in {
      val lastModified = Metadata("Date last modified", "12-12-2012")
      val language = Metadata("Language", "Unknown")
      val fileRow1 = FileRow("file_a", List(lastModified, language))
      val fileRow2 = FileRow("file_b", List(lastModified, language))
      val errors: Map[String, Seq[ValidationError]] = MetadataValidationJsonSchema.validate(Set(BASE_SCHEMA, CLOSURE_SCHEMA), List(fileRow1, fileRow2))
      errors.size shouldBe 2
      errors("file_a").size shouldBe 2
      errors("file_a") should contain(ValidationError(SCHEMA_BASE, "language", "enum"))
      errors("file_a") should contain(ValidationError(SCHEMA_BASE, "date_last_modified", "format.date"))
      errors("file_b").size shouldBe 2
      errors("file_b") should contain(ValidationError(SCHEMA_BASE, "language", "enum"))
      errors("file_b") should contain(ValidationError(SCHEMA_BASE, "date_last_modified", "format.date"))
    }

    "validate file rows that produce error from base schema and closure" in {
      val lastModified = Metadata("Date last modified", "12-12-2012")
      val closureStartDateError = Metadata("Closure Start Date", "12-12-2001")
      val closureStatus = Metadata("Closure status", "Open")
      val fileRow1 = FileRow("file_a", List(lastModified, closureStatus, closureStartDateError))
      val errors: Map[String, Seq[ValidationError]] = MetadataValidationJsonSchema.validate(Set(BASE_SCHEMA, CLOSURE_SCHEMA), List(fileRow1))
      errors.size shouldBe 1
      errors("file_a") should contain(ValidationError(SCHEMA_BASE, "date_last_modified", "format.date"))
      errors("file_a") should contain(ValidationError(SCHEMA_BASE, "closure_start_date", "format.date"))
      errors("file_a") should contain(ValidationError(SCHEMA_CLOSURE, "closure_start_date", "maxLength"))
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

  private def singleErrorCheck(
      validationErrors: Map[String, List[ValidationError]],
      propertyName: String,
      value: Any,
      validationProcess: ValidationProcess
  ): Unit = {
    validationErrors.foreach(objectIdentifierWithErrors =>
      objectIdentifierWithErrors._2.foreach(error => {
        error.property shouldBe propertyName
        error.errorKey shouldBe value
        error.validationProcess shouldBe validationProcess

      })
    )
  }
}
