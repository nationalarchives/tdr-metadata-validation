package uk.gov.nationalarchives.tdr.validation.schema.fields

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.nationalarchives.tdr.validation.schema.helpers.TestHelper._
import uk.gov.nationalarchives.tdr.validation.schema.ValidationError
import uk.gov.nationalarchives.tdr.validation.schema.ValidationProcess.{SCHEMA_BASE, SCHEMA_REQUIRED, SCHEMA_CLOSURE_OPEN, SCHEMA_CLOSURE_CLOSED}

class ClosureStartDateSpec extends AnyWordSpecLike {

  "When validating against all schema we see" should {

    "success if no value is provided for an open record" in {
      val openTestFileRow = openMetadataFileRow(closureStartDate = Some(""))
      validationErrors(openTestFileRow).size shouldBe 0
    }

    "success if a value in the past is provided for a closed record" in {
      val closedTestFileRow = closedMetadataFileRow(closureStartDate = Some("2024-12-25"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "success if the closure_start_date matches the end_date" in {
      val closedTestFileRow = closedMetadataFileRow(closureStartDate = Some("2024-12-25"), dateOfRecord = Some("2024-12-25"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "success if the closure_start_date matches to 31st Dec of the year of end_date" in {
      val closedTestFileRow = closedMetadataFileRow(closureStartDate = Some("2024-12-31"), dateOfRecord = Some("2024-12-25"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "success if the closure_start_date matches to date_last_modified when no end_date provided" in {
      val closedTestFileRow = closedMetadataFileRow(closureStartDate = Some("2024-05-25"), dateLastModified = Some("2024-05-25"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "success if the closure_start_date matches to 31st Dec of the year of date_last_modified when no end_date provided" in {
      val closedTestFileRow = closedMetadataFileRow(closureStartDate = Some("2024-12-31"), dateLastModified = Some("2024-05-25"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "error(s) if the closure_start_date doesn't match the end_date but matches the date_last_modified" in {
      val closedTestFileRow = closedMetadataFileRow(closureStartDate = Some("2024-12-25"), dateOfRecord = Some("2023-02-25"), dateLastModified = Some("2024-12-25"))
      validationErrors(closedTestFileRow).size shouldBe 1
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "closure_start_date", "matchEndDateOrDateLastModified")
      )
    }

    "error(s) if the closure_start_date doesn't match the end_date or its 31st Dec of the year" in {
      val closedTestFileRow = closedMetadataFileRow(closureStartDate = Some("2024-12-31"), dateOfRecord = Some("2023-04-25"))
      validationErrors(closedTestFileRow).size shouldBe 1
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "closure_start_date", "matchEndDateOrDateLastModified")
      )
    }

    "error(s) if the closure_start_date doesn't match the date_last_modified or its 31st Dec of the year" in {
      val closedTestFileRow = closedMetadataFileRow(closureStartDate = Some("2024-12-31"), dateLastModified = Some("2023-04-25"))
      validationErrors(closedTestFileRow).size shouldBe 1
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "closure_start_date", "matchEndDateOrDateLastModified")
      )
    }

    "error(s) if the column is missing" in {
      val openTestFileRow = openMetadataFileRow(closureStartDate = None)
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_REQUIRED, "closure_start_date", "required")
      )
      val closedTestFileRow = closedMetadataFileRow(closureStartDate = None)
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_REQUIRED, "closure_start_date", "required")
      )
    }

    "error(s) if a value is provided for an open record" in {
      val openTestFileRow = openMetadataFileRow(closureStartDate = Some("2024-12-25"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_OPEN, "closure_start_date", "type") // Must be empty for an open record
      )
    }

    "error(s) if a value is provided for an open record, and the value is invalid (in future)" in {
      val openTestFileRow = openMetadataFileRow(closureStartDate = Some("3024-12-25"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_OPEN, "closure_start_date", "type"), // Must be empty for an open record
        ValidationError(SCHEMA_BASE, "closure_start_date", "daBeforeToday"), // This date is in the future, please correct //TODO: look at 2 x msgs
        ValidationError(SCHEMA_BASE, "closure_start_date", "matchEndDateOrDateLastModified") // Match end_date or date last modified
      )
    }

    "error(s) if a value is provided for an open record, and the value is invalid (not a date)" in {
      val openTestFileRow = openMetadataFileRow(closureStartDate = Some("xmas last year"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_OPEN, "closure_start_date", "type"), // Must be empty for an open record //TODO: look at 2 x msgs
        ValidationError(SCHEMA_BASE, "closure_start_date", "format.date") // We are unable to recognise this as a valid date format, please provide a date in the format yyyy-mm-dd
      )
    }

    "error(s) if a value is provided for an open record, and the value is invalid (alt date format yyyy-dd-mm)" in {
      val openTestFileRow = openMetadataFileRow(closureStartDate = Some("2024-25-12"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_OPEN, "closure_start_date", "type"), // Must be empty for an open record //TODO: look at 2 x msgs
        ValidationError(SCHEMA_BASE, "closure_start_date", "format.date") // We are unable to recognise this as a valid date format, please provide a date in the format yyyy-mm-dd
      )
    }

    "error(s) if a value is provided for an open record, and the value is invalid (alt date format slash)" in {
      val openTestFileRow = openMetadataFileRow(closureStartDate = Some("25/12/2024"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_OPEN, "closure_start_date", "type"), // Must be empty for an open record //TODO: look at 2 x msgs
        ValidationError(SCHEMA_BASE, "closure_start_date", "format.date") // We are unable to recognise this as a valid date format, please provide a date in the format yyyy-mm-dd
      )
    }

    "error(s) if the value is missing for a closed record" in {
      val closedTestFileRow = closedMetadataFileRow(closureStartDate = Some(""))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_CLOSED, "closure_start_date", "type") // Must be provided for a closed record
      )
    }

    "error(s) if the value is invalid (in future)" in {
      val closedTestFileRow = closedMetadataFileRow(closureStartDate = Some("3024-12-25"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "closure_start_date", "daBeforeToday"), // This date is in the future, please correct
        ValidationError(SCHEMA_BASE, "closure_start_date", "matchEndDateOrDateLastModified")
      )
    }

    "error(s) if the value is invalid (not a date)" in {
      val closedTestFileRow = closedMetadataFileRow(closureStartDate = Some("xmas last year"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "closure_start_date", "format.date") // We are unable to recognise this as a valid date format, please provide a date in the format yyyy-mm-dd
      )
    }

    "error(s) if the value is invalid (alt date format yyyy-dd-mm)" in {
      val closedTestFileRow = closedMetadataFileRow(closureStartDate = Some("2024-25-12"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "closure_start_date", "format.date") // We are unable to recognise this as a valid date format, please provide a date in the format yyyy-mm-dd
      )
    }

    "error(s) if the value is invalid (alt date format slash)" in {
      val closedTestFileRow = closedMetadataFileRow(closureStartDate = Some("25/12/2024"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "closure_start_date", "format.date") // We are unable to recognise this as a valid date format, please provide a date in the format yyyy-mm-dd
      )
    }

    "error(s) if the value is invalid (a boolean)" in {
      val closedTestFileRow = closedMetadataFileRow(closureStartDate = Some("Yes"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "closure_start_date", "format.date") // We are unable to recognise this as a valid date format, please provide a date in the format yyyy-mm-dd
      )
    }
  }
}
