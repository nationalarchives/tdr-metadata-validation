package uk.gov.nationalarchives.tdr.validation

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.testkit.{ImplicitSender, TestKit}
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaDefinition._
import uk.gov.nationalarchives.tdr.validation.schema.MetadataValidationJsonSchema.ObjectMetadata
import uk.gov.nationalarchives.tdr.validation.schema.ValidationProcess.{SCHEMA_BASE, _}
import uk.gov.nationalarchives.tdr.validation.schema.{MetadataValidationJsonSchema, ValidationError}

class MetadataValidationJsonSchemaSpec extends TestKit(ActorSystem("MetadataValidationJsonSchemaSpec")) with ImplicitSender with AnyWordSpecLike {

  "MetadataValidationJsonSchema with BASE_SCHEMA" should {
    "validate boolean field with true/false value" in {
      val objectMetadata = Set(
        ObjectMetadata(
          "file1",
          Set(
            Metadata("judgment_type", "judgment"),
            Metadata("judgment_update", "true")
          )
        )
      )
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, objectMetadata)
      validationErrors("file1").size shouldBe 0
    }

    "validate incorrect value in enumerated array" in {
      val data: Set[ObjectMetadata] = dataBuilder("language", "Unknown")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "language", "enum", SCHEMA_BASE)
    }
    "validate correct value enumerated array" in {
      val data: Set[ObjectMetadata] = dataBuilder("language", "Welsh")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }
    "validate array can be null" in {
      val data: Set[ObjectMetadata] = dataBuilder("translated filename", "")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }
    "validate incorrect date format" in {
      val data: Set[ObjectMetadata] = dataBuilder("date last modified", "12-12-2012")
      val validationErrors: Map[String, List[ValidationError]] = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "date_last_modified", "format.date", SCHEMA_BASE)
    }
    "validate correct date format" in {
      val data: Set[ObjectMetadata] = dataBuilder("date last modified", "2023-12-05")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }
    "validate date last modified must have a value" in {
      val data: Set[ObjectMetadata] = dataBuilder("date last modified", "")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "date_last_modified", "type", SCHEMA_BASE)
    }
    "validate end date can be empty" in {
      val data: Set[ObjectMetadata] = dataBuilder("date of the record", "")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }
    "validate end date must be before today" in {
      val data: Set[ObjectMetadata] = dataBuilder("date of the record", "3000-12-12")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "end_date", "daBeforeToday", SCHEMA_BASE)
    }
    "validate incorrect value for a date field which has daBeforeToday condition" in {
      val data: Set[ObjectMetadata] = dataBuilder("date of the record", "eeee")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "end_date", "format.date", SCHEMA_BASE)
    }
    "validate description field with some value" in {
      val data: Set[ObjectMetadata] = dataBuilder("description", "ggg")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }

    "validate description field with empty value" in {
      val data: Set[ObjectMetadata] = dataBuilder("description", "")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }

    "validate closure period must be a number" in {
      val data: Set[ObjectMetadata] = dataBuilder("closure period", "123")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }
    "validate closure period must be less than 150" in {
      val data: Set[ObjectMetadata] = dataBuilder("closure period", "151")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "closure_period", "maximum", SCHEMA_BASE)
    }
    "validate closure period must be at least 1" in {
      val data: Set[ObjectMetadata] = dataBuilder("closure period", "0")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "closure_period", "minimum", SCHEMA_BASE)
    }
    "validate closure period can be 150" in {
      val data: Set[ObjectMetadata] = dataBuilder("closure period", "150")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }
    "validate closure period can be 1" in {
      val data: Set[ObjectMetadata] = dataBuilder("closure period", "1")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }
    "validate title closed is a boolean" in {
      val data: Set[ObjectMetadata] = dataBuilder("is filename closed", "Yes")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }
    "validate title closed not yes/no" in {
      val data: Set[ObjectMetadata] = dataBuilder("is filename closed", "blah")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "title_closed", "type", SCHEMA_BASE)
    }
    "validate file path ok with one character" in {
      val data: Set[ObjectMetadata] = dataBuilder("filepath", "b")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }
    "validate file path must have content" in {
      val data: Set[ObjectMetadata] = dataBuilder("filepath", "")
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "file_path", "type", SCHEMA_BASE)
    }

    "validate former_reference_department can have max 255 characters" in {
      val data: Set[ObjectMetadata] = dataBuilder(
        "former_reference_department",
        "This is an example. This is an example. This is an example. This is an example. This is an example. This is an example. This is an example. This is an example. This is an example. This is an example. This is an example. This is an example. This is an example."
      )
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "former_reference_department", "maxLength", SCHEMA_BASE)
    }

    "validate former_reference_department can't have line breaks" in {
      val data: Set[ObjectMetadata] = dataBuilder(
        "former_reference_department",
        "This is an example.\nwith line break"
      )
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "former_reference_department", "pattern", SCHEMA_BASE)
    }

    "validate file_name_translation can't have line breaks" in {
      val data: Set[ObjectMetadata] = dataBuilder(
        "file_name_translation",
        "This is an example.\nwith line break"
      )
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "file_name_translation", "pattern", SCHEMA_BASE)
    }
    "validate title_alternate can't have line breaks" in {
      val data: Set[ObjectMetadata] = dataBuilder(
        "title_alternate",
        "This is an example.\nwith line break"
      )
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "title_alternate", "pattern", SCHEMA_BASE)
    }

    "validate copyright can contain single valid value" in {
      val data: Set[ObjectMetadata] = dataBuilder(
        "copyright",
        "Third party"
      )
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }

    "validate copyright can contain multiple valid values" in {
      val data: Set[ObjectMetadata] = dataBuilder(
        "copyright",
        "Crown copyright;Third party"
      )
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }

    "validate copyright must contain a correct value" in {
      val data: Set[ObjectMetadata] = dataBuilder(
        "copyright",
        "some random value"
      )
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "rights_copyright", "enum", SCHEMA_BASE)
    }

    "validate restrictions_on_use can be empty" in {
      val data: Set[ObjectMetadata] = dataBuilder(
        "restrictions_on_use",
        ""
      )
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }

    "validate restrictions_on_use cannot be more than 500 characters" in {
      val data: Set[ObjectMetadata] = dataBuilder(
        "restrictions_on_use",
        "more than 500 characters more than 500 characters more than 500 characters more than 500 characters more than 500 characters" +
          " more than 500 characters more than 500 characters more than 500 characters more than 500 characters" +
          " more than 500 characters more than 500 characters more than 500 characters more than 500 characters " +
          "more than 500 characters more than 500 characters more than 500 characters more than 500 characters more than 500 characters" +
          " more than 500 characters more than 500 characters more than 500 characters"
      )
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "restrictions_on_use", "maxLength", SCHEMA_BASE)
    }
  }

  "MetadataValidationJsonSchema with CLOSURE_SCHEMA" should {

    "return no errors if all fields have valid values when closure_type is Closed" in {
      val data: Set[ObjectMetadata] = closureDataBuilder(
        closureType = Some("Closed"),
        closureStartDate = Some("2001-12-12"),
        closurePeriod = Some("5"),
        foiExemptionAsserted = Some("2001-12-12"),
        foiCodes = Some("27(1)"),
        titleClosed = Some("Yes"),
        titleAlternative = Some("alternate filename"),
        descriptionClosed = Some("No")
      )
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(CLOSURE_SCHEMA_CLOSED, data)
      validationErrors("file1").size shouldBe 0
    }

    "return no errors if all fields have valid values when closure_type is Open" in {
      val data: Set[ObjectMetadata] = closureDataBuilder(
        closureType = Some("Open"),
        descriptionClosed = Some("No"),
        titleClosed = Some("No")
      )
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(CLOSURE_SCHEMA_OPEN, data)
      validationErrors("file1").size shouldBe 0
    }

    "not return any errors from Closure Schema when closure_type is Open and title/desc are open and start date empty" in {
      val data: Set[ObjectMetadata] = closureDataBuilder(
        closureType = Some("Open"),
        descriptionClosed = Some("No"),
        titleClosed = Some("No")
      )

      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(CLOSURE_SCHEMA_OPEN, data)
      validationErrors("file1") shouldBe List.empty
    }
    "return any errors from Closure Schema when closure_type is Open and title/desc are open and start date is not empty" in {
      val data: Set[ObjectMetadata] = closureDataBuilder(
        closureType = Some("Open"),
        descriptionClosed = Some("No"),
        titleClosed = Some("No"),
        closureStartDate = Some("2012-12-12")
      )
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(CLOSURE_SCHEMA_OPEN, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "closure_start_date", "type", SCHEMA_CLOSURE_OPEN)
    }

    "return errors with all the required fields when closure_type is Closed" in {
      val closure = Metadata("closure_type", "Closed")
      val data: Set[ObjectMetadata] = Set(ObjectMetadata("file1", Set(closure)))
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(CLOSURE_SCHEMA_CLOSED, data)
      validationErrors("file1").size shouldBe 4
      validationErrors("file1") should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_CLOSED, "title_alternate", "required"),
        ValidationError(SCHEMA_CLOSURE_CLOSED, "title_closed", "required"),
        ValidationError(SCHEMA_CLOSURE_CLOSED, "description_alternate", "required"),
        ValidationError(SCHEMA_CLOSURE_CLOSED, "description_closed", "required")
      )
    }

    "return errors if fields have invalid values when closure_type is Closed" in {
      val data: Set[ObjectMetadata] = closureDataBuilder(
        closureType = Some("Closed"),
        titleClosed = Some("Yes"),
        descriptionClosed = Some("N")
      )
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(CLOSURE_SCHEMA_CLOSED, data)

      validationErrors("file1").size shouldBe 6
      validationErrors("file1") should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_CLOSED, "foi_exemption_code", "type"),
        ValidationError(SCHEMA_CLOSURE_CLOSED, "closure_period", "type"),
        ValidationError(SCHEMA_CLOSURE_CLOSED, "foi_exemption_asserted", "type"),
        ValidationError(SCHEMA_CLOSURE_CLOSED, "description_closed", "type"),
        ValidationError(SCHEMA_CLOSURE_CLOSED, "title_alternate", "type"),
        ValidationError(SCHEMA_CLOSURE_CLOSED, "closure_start_date", "type")
      )
    }

    "return errors if required fields has invalid values when closure_type is Open" in {
      val data: Set[ObjectMetadata] = closureDataBuilder(
        closureType = Some("Open"),
        closureStartDate = Some("2000"),
        closurePeriod = Some("-100"),
        foiExemptionAsserted = Some("1/2/1234"),
        foiCodes = Some("abc"),
        titleClosed = Some("Yes"),
        descriptionClosed = Some("No")
      )
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(CLOSURE_SCHEMA_OPEN, data)

      validationErrors("file1").size shouldBe 5
      validationErrors("file1") should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_OPEN, "title_closed", "const"),
        ValidationError(SCHEMA_CLOSURE_OPEN, "foi_exemption_asserted", "type"),
        ValidationError(SCHEMA_CLOSURE_OPEN, "closure_start_date", "type"),
        ValidationError(SCHEMA_CLOSURE_OPEN, "closure_period", "type"),
        ValidationError(SCHEMA_CLOSURE_OPEN, "foi_exemption_code", "type")
      )
    }

    "return errors if required fields have empty values when closure_type is Closed" in {
      val data: Set[ObjectMetadata] = closureDataBuilder(closureType = Some("Closed"))
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(CLOSURE_SCHEMA_CLOSED, data)
      validationErrors("file1").size shouldBe 6
      validationErrors("file1") should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_CLOSED, "closure_start_date", "type"),
        ValidationError(SCHEMA_CLOSURE_CLOSED, "foi_exemption_code", "type"),
        ValidationError(SCHEMA_CLOSURE_CLOSED, "closure_period", "type"),
        ValidationError(SCHEMA_CLOSURE_CLOSED, "foi_exemption_asserted", "type"),
        ValidationError(SCHEMA_CLOSURE_CLOSED, "description_closed", "type"),
        ValidationError(SCHEMA_CLOSURE_CLOSED, "title_closed", "type")
      )
    }

    "return no errors if fields have empty values when closure_type is Open" in {
      val data: Set[ObjectMetadata] = closureDataBuilder(closureType = Some("OPEN"))
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(CLOSURE_SCHEMA_OPEN, data)
      validationErrors("file1").size shouldBe 0
    }

    "return no errors if title/description closed is false and an alternate filename/description is not provided when closure_type is Closed" in {
      val data: Set[ObjectMetadata] = closureDataBuilder(
        closureType = Some("Closed"),
        closureStartDate = Some("2001-12-12"),
        closurePeriod = Some("5"),
        foiExemptionAsserted = Some("2001-12-12"),
        foiCodes = Some("27(1)"),
        titleClosed = Some("No"),
        descriptionClosed = Some("No")
      )
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(CLOSURE_SCHEMA_CLOSED, data)
      validationErrors("file1").size shouldBe 0
    }

    "return no errors if title/description closed is true an alternate filename/description is provided when closure_type is Closed" in {
      val data: Set[ObjectMetadata] = closureDataBuilder(
        closureType = Some("Closed"),
        closureStartDate = Some("2001-12-12"),
        closurePeriod = Some("5"),
        foiExemptionAsserted = Some("2001-12-12"),
        foiCodes = Some("27(1)"),
        titleClosed = Some("Yes"),
        titleAlternative = Some("alt title"),
        descriptionClosed = Some("Yes"),
        descriptionAlternative = Some("alt desc")
      )
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(CLOSURE_SCHEMA_CLOSED, data)
      validationErrors("file1").size shouldBe 0
    }

    "return errors if title/description closed is true but an alternate filename/description is not provided when closure_type is Closed" in {
      val data: Set[ObjectMetadata] = closureDataBuilder(
        closureType = Some("Closed"),
        closureStartDate = Some("2001-12-12"),
        closurePeriod = Some("5"),
        foiExemptionAsserted = Some("2001-12-12"),
        foiCodes = Some("27(1)"),
        titleClosed = Some("Yes"),
        descriptionClosed = Some("Yes")
      )
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(CLOSURE_SCHEMA_CLOSED, data)
      validationErrors("file1").size shouldBe 2
      validationErrors("file1") should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_CLOSED, "title_alternate", "type"),
        ValidationError(SCHEMA_CLOSURE_CLOSED, "description_alternate", "type")
      )
    }

    "don't allow title/description closed to be null for closed" in {
      val data: Set[ObjectMetadata] = closureDataBuilder(
        closureType = Some("Closed"),
        closureStartDate = Some("2001-12-12"),
        closurePeriod = Some("5"),
        foiExemptionAsserted = Some("2001-12-12"),
        foiCodes = Some("27(1)"),
        titleClosed = None,
        descriptionClosed = Some(""),
        titleAlternative = Some(""),
        descriptionAlternative = None
      )
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(CLOSURE_SCHEMA_CLOSED, data)
      validationErrors("file1").size shouldBe 2
      validationErrors("file1") should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_CLOSED, "title_closed", "type"),
        ValidationError(SCHEMA_CLOSURE_CLOSED, "description_closed", "type")
      )
    }

    "don't allow title/description closed to be null for open" in {
      val data: Set[ObjectMetadata] = closureDataBuilder(
        closureType = Some("Open"),
        titleClosed = None,
        descriptionClosed = Some("")
      )
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(CLOSURE_SCHEMA_OPEN, data)
      validationErrors("file1").size shouldBe 2
      validationErrors("file1") should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_OPEN, "title_closed", "const"),
        ValidationError(SCHEMA_CLOSURE_OPEN, "description_closed", "const")
      )
    }

    "don't allow title/description closed to be true for open" in {
      val data: Set[ObjectMetadata] = closureDataBuilder(
        closureType = Some("Open"),
        titleClosed = Some("Yes"),
        descriptionClosed = Some("Yes")
      )
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(CLOSURE_SCHEMA_OPEN, data)
      validationErrors("file1").size shouldBe 2
      validationErrors("file1") should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_OPEN, "title_closed", "const"),
        ValidationError(SCHEMA_CLOSURE_OPEN, "description_closed", "const")
      )
    }

    "don't allow title/description closed to be null for base" in {
      val data: Set[ObjectMetadata] = closureDataBuilder(
        closureType = Some("Open"),
        titleClosed = None,
        descriptionClosed = Some("")
      )
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(BASE_SCHEMA, data)
      validationErrors("file1").size shouldBe 2
      validationErrors("file1") should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "title_closed", "type"),
        ValidationError(SCHEMA_BASE, "description_closed", "type")
      )
    }

    "return errors if title/description closed is false but an alternate filename/description is provided when closure_type is Closed" in {
      val data: Set[ObjectMetadata] = closureDataBuilder(
        closureType = Some("Closed"),
        closureStartDate = Some("2001-12-12"),
        closurePeriod = Some("5"),
        foiExemptionAsserted = Some("2001-12-12"),
        foiCodes = Some("27(1)"),
        titleClosed = Some("No"),
        titleAlternative = Some("alt title"),
        descriptionClosed = Some("No"),
        descriptionAlternative = Some("alt desc")
      )
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(CLOSURE_SCHEMA_CLOSED, data)
      validationErrors("file1").size shouldBe 2
      validationErrors("file1") should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_CLOSED, "title_closed", "const"),
        ValidationError(SCHEMA_CLOSURE_CLOSED, "description_closed", "const")
      )
    }

    "MetadataValidationJsonSchema validate with both BASE_SCHEMA, CLOSURE_SCHEMA_CLOSED and CLOSURE_SCHEMA_OPEN" should {
      "validate file rows that only have base schema errors" in {
        val data = closureDataBuilder(
          closureType = Some("Closed"),
          closurePeriod = Some("5"),
          titleClosed = Some("no"),
          foiCodes = Some("27(1)"),
          foiExemptionAsserted = Some("2001-12-12"),
          closureStartDate = Some("2001-12-12"),
          descriptionClosed = Some("no")
        ).flatMap(_.metadata).toList
        val lastModified = Metadata("date last modified", "12-12-2012")
        val language = Metadata("language", "Unknown")
        val fileRow1 = FileRow("file_a", List(lastModified, language) ++ data)
        val fileRow2 = FileRow("file_b", List(lastModified, language) ++ data)
        val errors: Map[String, Seq[ValidationError]] =
          MetadataValidationJsonSchema.validate(Set(BASE_SCHEMA, CLOSURE_SCHEMA_CLOSED, CLOSURE_SCHEMA_OPEN), List(fileRow1, fileRow2))
        errors.size shouldBe 2
        errors("file_a").size shouldBe 2
        errors("file_a") should contain(ValidationError(SCHEMA_BASE, "language", "enum"))
        errors("file_a") should contain(ValidationError(SCHEMA_BASE, "date_last_modified", "format.date"))
        errors("file_b").size shouldBe 2
        errors("file_b") should contain(ValidationError(SCHEMA_BASE, "language", "enum"))
        errors("file_b") should contain(ValidationError(SCHEMA_BASE, "date_last_modified", "format.date"))
      }
    }

    "validate file rows that produce error from base schema and closure_open schema" in {
      val data = closureDataBuilder(
        closureType = Some("Open"),
        closureStartDate = Some("12-12-2001"),
        closurePeriod = Some("5"),
        titleClosed = Some("N")
      ).flatMap(_.metadata).toList
      val lastModified = Metadata("date last modified", "12-12-2012")
      val fileRow1 = FileRow("file_a", List(lastModified) ++ data)
      val errors: Map[String, Seq[ValidationError]] = MetadataValidationJsonSchema.validate(Set(BASE_SCHEMA, CLOSURE_SCHEMA_CLOSED, CLOSURE_SCHEMA_OPEN), List(fileRow1))
      errors.size shouldBe 1
      errors("file_a") should contain(ValidationError(SCHEMA_BASE, "date_last_modified", "format.date"))
      errors("file_a") should contain(ValidationError(SCHEMA_BASE, "closure_start_date", "format.date"))
      errors("file_a") should contain(ValidationError(SCHEMA_CLOSURE_OPEN, "closure_start_date", "type"))
      errors("file_a") should contain(ValidationError(SCHEMA_CLOSURE_OPEN, "closure_period", "type"))
      errors("file_a") should contain(ValidationError(SCHEMA_CLOSURE_OPEN, "title_closed", "const"))
    }

    "validate file rows that produce error from base schema and closure_closed schema" in {
      val data = closureDataBuilder(
        closureType = Some("Closed"),
        closurePeriod = Some("-99"),
        titleClosed = Some("no"),
        foiCodes = Some("78|27(1)|27(2)"),
        foiExemptionAsserted = Some("2001-12-12"),
        closureStartDate = Some("3001-12-12"),
        descriptionClosed = Some("yes")
      ).flatMap(_.metadata).toList
      val lastModified = Metadata("date last modified", "12-12-2012")
      val fileRow1 = FileRow("file_a", List(lastModified) ++ data)
      val errors: Map[String, Seq[ValidationError]] = MetadataValidationJsonSchema.validate(Set(BASE_SCHEMA, CLOSURE_SCHEMA_CLOSED, CLOSURE_SCHEMA_OPEN), List(fileRow1))
      errors.size shouldBe 1
      errors("file_a").size shouldBe 5
      errors("file_a") should contain(ValidationError(SCHEMA_BASE, "date_last_modified", "format.date"))
      errors("file_a") should contain(ValidationError(SCHEMA_BASE, "closure_period", "minimum"))
      errors("file_a") should contain(ValidationError(SCHEMA_BASE, "foi_exemption_code", "enum"))
      errors("file_a") should contain(ValidationError(SCHEMA_CLOSURE_CLOSED, "description_alternate", "type"))
      errors("file_a") should contain(ValidationError(SCHEMA_BASE, "closure_start_date", "daBeforeToday"))
    }
  }
  "MetadataValidationJsonSchema validate with REQUIRED_SCHEMA" should {
    "validate the absent required fields and return errors" in {
      val data = closureDataBuilder(
        closureType = Some("Closed"),
        closurePeriod = Some("5"),
        titleClosed = Some("no"),
        foiCodes = Some("27(1)"),
        foiExemptionAsserted = Some("2001-12-12"),
        closureStartDate = Some("2001-12-12"),
        descriptionClosed = Some("no"),
        fileNameTranslation = Some("translated file name")
      ).flatMap(_.metadata).toList
      val lastModified = Metadata("date last modified", "12-12-2012")
      val language = Metadata("language", "Unknown")
      val fileRow1 = FileRow("file_a", List(lastModified, language) ++ data)
      val errors: Map[String, Seq[ValidationError]] =
        MetadataValidationJsonSchema.validate(Set(REQUIRED_SCHEMA), List(fileRow1))
      errors.size shouldBe 1
      errors("file_a").size shouldBe 4
      errors("file_a") should contain(ValidationError(SCHEMA_REQUIRED, "file_path", "required"))
      errors("file_a") should contain(ValidationError(SCHEMA_REQUIRED, "end_date", "required"))
      errors("file_a") should contain(ValidationError(SCHEMA_REQUIRED, "description", "required"))
      errors("file_a") should contain(ValidationError(SCHEMA_REQUIRED, "rights_copyright", "required"))
    }
  }
  "MetadataValidationJsonSchema with RELATIONSHIP_SCHEMA" should {
    "validate error when no description and description alternate provided" in {
      val values = List(("alternate description", "set alternate description"), ("description", ""))
      val data: Set[ObjectMetadata] = dataBuilderMultipleValues(values)
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(RELATIONSHIP_SCHEMA, data)
      validationErrors("file1").size shouldBe 1
      singleErrorCheck(validationErrors, "description", "type", SCHEMA_RELATIONSHIP)
    }
    "validate valid when no description and no description alternate" in {
      val values = List(("alternate description", ""), ("description", ""))
      val data: Set[ObjectMetadata] = dataBuilderMultipleValues(values)
      val validationErrors = MetadataValidationJsonSchema.validateWithSingleSchema(RELATIONSHIP_SCHEMA, data)
      validationErrors("file1").size shouldBe 0
    }
  }

  private def dataBuilder(key: String, value: String): Set[ObjectMetadata] = {
    val metadata = Metadata(key, value)
    Set(ObjectMetadata("file1", Set(metadata)))
  }

  private def dataBuilderMultipleValues(data: List[(String, String)]): Set[ObjectMetadata] = {
    val metadata = data.map(value => Metadata(value._1, value._2))
    Set(ObjectMetadata("file1", metadata.toSet))
  }

  private def closureDataBuilder(
      closureType: Option[String] = None,
      closureStartDate: Option[String] = None,
      closurePeriod: Option[String] = None,
      foiExemptionAsserted: Option[String] = None,
      foiCodes: Option[String] = None,
      titleClosed: Option[String] = None,
      titleAlternative: Option[String] = None,
      descriptionClosed: Option[String] = None,
      descriptionAlternative: Option[String] = None,
      fileNameTranslation: Option[String] = None
  ): Set[ObjectMetadata] = {
    Set(
      ObjectMetadata(
        "file1",
        Set(
          Metadata("closure status", closureType.getOrElse("")), // closure_status
          Metadata("closure start date", closureStartDate.getOrElse("")), // closure_start_date
          Metadata("foi exemption code", foiCodes.getOrElse("")), // foi_exemption_code
          Metadata("closure period", closurePeriod.getOrElse("")), // closure_period
          Metadata("foi schedule date", foiExemptionAsserted.getOrElse("")), // foi_exemption_asserted
          Metadata("is filename closed", titleClosed.getOrElse("")), // title_closed
          Metadata("alternate filename", titleAlternative.getOrElse("")), // title_alternative
          Metadata("is description closed", descriptionClosed.getOrElse("")), // description_closed
          Metadata("alternate description", descriptionAlternative.getOrElse("")), // description_alternate
          Metadata("translated filename", fileNameTranslation.getOrElse("")) // file_name_translation
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
