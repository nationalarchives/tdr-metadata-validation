package uk.gov.nationalarchives.tdr.validation.schema.fields

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.nationalarchives.tdr.validation.schema.ValidationError
import uk.gov.nationalarchives.tdr.validation.schema.ValidationProcess.{SCHEMA_BASE, SCHEMA_REQUIRED}
import uk.gov.nationalarchives.tdr.validation.schema.helpers.TestHelper._

class EndDateSpec extends AnyWordSpecLike {

  "When validating against all schema we see" should {

    "success if a value (in the past) is provided for an open record" in {
      val openTestFileRow = openMetadataFileRow(dateOfRecord = Some("2024-12-25"))
      validationErrors(openTestFileRow).size shouldBe 0
    }

    "success if a value is provided for a closed record" in {
      val closedTestFileRow = closedMetadataFileRow(dateOfRecord = Some("2024-12-25"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "success is the cell is empty" in {
      val openTestFileRow = openMetadataFileRow(dateOfRecord = Some(""))
      validationErrors(openTestFileRow).size shouldBe 0

      val closedTestFileRow = closedMetadataFileRow(dateOfRecord = Some(""))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "error(s) if the column is missing" in {
      val openTestFileRow = openMetadataFileRow(dateOfRecord = None)
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_REQUIRED, "end_date", "required")
      )

      val closedTestFileRow = closedMetadataFileRow(dateOfRecord = None)
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_REQUIRED, "end_date", "required")
      )
    }

    "error(s) if the value is invalid (in future)" in {
      val openTestFileRow = openMetadataFileRow(dateOfRecord = Some("3024-12-25"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "end_date", "daBeforeToday") // This date is in the future, please correct
      )
      val closedTestFileRow = closedMetadataFileRow(dateOfRecord = Some("3024-12-25"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "end_date", "daBeforeToday"), // This date is in the future, please correct
        ValidationError(SCHEMA_BASE, "closure_start_date", "matchEndDateOrDateLastModified")
      )
    }

    "error(s) if the value is invalid (not a date)" in {
      val openTestFileRow = openMetadataFileRow(dateOfRecord = Some("xmas last year"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "end_date", "format.date") // We are unable to recognise this as a valid date format, please provide a date in the format yyyy-mm-dd
      )
      val closedTestFileRow = closedMetadataFileRow(dateOfRecord = Some("xmas last year"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "end_date", "format.date") // We are unable to recognise this as a valid date format, please provide a date in the format yyyy-mm-dd
      )
    }

    "error(s) if the value is invalid (alt date format yyyy-dd-mm)" in {
      val openTestFileRow = openMetadataFileRow(dateOfRecord = Some("2024-25-12"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "end_date", "format.date") // We are unable to recognise this as a valid date format, please provide a date in the format yyyy-mm-dd
      )
      val closedTestFileRow = closedMetadataFileRow(dateOfRecord = Some("2024-25-12"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "end_date", "format.date") // We are unable to recognise this as a valid date format, please provide a date in the format yyyy-mm-dd
      )
    }

    "error(s) if the value is invalid (alt date format slash)" in {
      val openTestFileRow = openMetadataFileRow(dateOfRecord = Some("25/12/2024"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "end_date", "format.date") // We are unable to recognise this as a valid date format, please provide a date in the format yyyy-mm-dd
      )
      val closedTestFileRow = closedMetadataFileRow(dateOfRecord = Some("25/12/2024"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "end_date", "format.date") // We are unable to recognise this as a valid date format, please provide a date in the format yyyy-mm-dd
      )
    }

    "error(s) if the value is invalid (a boolean)" in {
      val openTestFileRow = openMetadataFileRow(dateOfRecord = Some("Yes"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "end_date", "format.date") // We are unable to recognise this as a valid date format, please provide a date in the format yyyy-mm-dd
      )
      val closedTestFileRow = closedMetadataFileRow(dateOfRecord = Some("Yes"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "end_date", "format.date") // We are unable to recognise this as a valid date format, please provide a date in the format yyyy-mm-dd
      )
    }
  }
}
