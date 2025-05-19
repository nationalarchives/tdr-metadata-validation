package uk.gov.nationalarchives.tdr.validation.schema.fields

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.nationalarchives.tdr.validation.schema.ValidationError
import uk.gov.nationalarchives.tdr.validation.schema.ValidationProcess.{SCHEMA_BASE, SCHEMA_CLOSURE_CLOSED, SCHEMA_CLOSURE_OPEN, SCHEMA_REQUIRED}
import uk.gov.nationalarchives.tdr.validation.schema.helpers.TestHelper._

class ExemptionAssertedSpec extends AnyWordSpecLike {

  "When validating against all schema we see " should {

    "success if no value is provided for an open record" in {
      val openTestFileRow = openMetadataFileRow(foiExemptionAsserted = Some(""))
      validationErrors(openTestFileRow).size shouldBe 0
    }

    "success if a value in the past is provided for a closed record" in {
      val closedTestFileRow = closedMetadataFileRow(foiExemptionAsserted = Some("2024-12-25"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "error(s) if the column is missing" in {
      val openTestFileRow = openMetadataFileRow(foiExemptionAsserted = None)
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_REQUIRED, "foi_exemption_asserted", "required")
      )
      val closedTestFileRow = closedMetadataFileRow(foiExemptionAsserted = None)
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_REQUIRED, "foi_exemption_asserted", "required")
      )
    }

    "error(s) if a value is provided for an open record" in {
      val openTestFileRow = openMetadataFileRow(foiExemptionAsserted = Some("2024-12-25"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_OPEN, "foi_exemption_asserted", "type") // Must be empty for an open record
      )
    }

    "error(s) if a value is provided for an open record, and the value is invalid (in future)" in {
      val openTestFileRow = openMetadataFileRow(foiExemptionAsserted = Some("3024-12-25"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_OPEN, "foi_exemption_asserted", "type"), // Must be empty for an open record
        ValidationError(SCHEMA_BASE, "foi_exemption_asserted", "daBeforeToday") // This date is in the future, please correct //TODO: look at 2 x msgs
      )
    }

    "error(s) if a value is provided for an open record, and the value is invalid (not a date)" in {
      val openTestFileRow = openMetadataFileRow(foiExemptionAsserted = Some("xmas last year"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_OPEN, "foi_exemption_asserted", "type"), // Must be empty for an open record //TODO: look at 2 x msgs
        ValidationError(
          SCHEMA_BASE,
          "foi_exemption_asserted",
          "format.date"
        ) // We are unable to recognise this as a valid date format, please provide a date in the format yyyy-mm-dd
      )
    }

    "error(s) if a value is provided for an open record, and the value is invalid (alt date format yyyy-dd-mm)" in {
      val openTestFileRow = openMetadataFileRow(foiExemptionAsserted = Some("2024-25-12"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_OPEN, "foi_exemption_asserted", "type"), // Must be empty for an open record //TODO: look at 2 x msgs
        ValidationError(
          SCHEMA_BASE,
          "foi_exemption_asserted",
          "format.date"
        ) // We are unable to recognise this as a valid date format, please provide a date in the format yyyy-mm-dd
      )
    }

    "error(s) if a value is provided for an open record, and the value is invalid (alt date format slash)" in {
      val openTestFileRow = openMetadataFileRow(foiExemptionAsserted = Some("25/12/2024"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_OPEN, "foi_exemption_asserted", "type"), // Must be empty for an open record //TODO: look at 2 x msgs
        ValidationError(
          SCHEMA_BASE,
          "foi_exemption_asserted",
          "format.date"
        ) // We are unable to recognise this as a valid date format, please provide a date in the format yyyy-mm-dd
      )
    }

    "error(s) if the value is missing for a closed record" in {
      val closedTestFileRow = closedMetadataFileRow(foiExemptionAsserted = Some(""))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_CLOSED, "foi_exemption_asserted", "type") // Must be provided for a closed record
      )
    }

    "error(s) if the value is invalid (in future)" in {
      val closedTestFileRow = closedMetadataFileRow(foiExemptionAsserted = Some("3024-12-25"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "foi_exemption_asserted", "daBeforeToday") // This date is in the future, please correct //TODO: look at 2 x msgs
      )
    }

    "error(s) if the value is invalid (not a date)" in {
      val closedTestFileRow = closedMetadataFileRow(foiExemptionAsserted = Some("xmas last year"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(
          SCHEMA_BASE,
          "foi_exemption_asserted",
          "format.date"
        ) // We are unable to recognise this as a valid date format, please provide a date in the format yyyy-mm-dd
      )
    }

    "error(s) if the value is invalid (alt date format yyyy-dd-mm)" in {
      val closedTestFileRow = closedMetadataFileRow(foiExemptionAsserted = Some("2024-25-12"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(
          SCHEMA_BASE,
          "foi_exemption_asserted",
          "format.date"
        ) // We are unable to recognise this as a valid date format, please provide a date in the format yyyy-mm-dd
      )
    }

    "error(s) if the value is invalid (alt date format slash)" in {
      val closedTestFileRow = closedMetadataFileRow(foiExemptionAsserted = Some("25/12/2024"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(
          SCHEMA_BASE,
          "foi_exemption_asserted",
          "format.date"
        ) // We are unable to recognise this as a valid date format, please provide a date in the format yyyy-mm-dd
      )
    }

    "error(s) if the value is invalid (a boolean)" in {
      val closedTestFileRow = closedMetadataFileRow(foiExemptionAsserted = Some("Yes"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(
          SCHEMA_BASE,
          "foi_exemption_asserted",
          "format.date"
        ) // We are unable to recognise this as a valid date format, please provide a date in the format yyyy-mm-dd
      )
    }
  }
}
