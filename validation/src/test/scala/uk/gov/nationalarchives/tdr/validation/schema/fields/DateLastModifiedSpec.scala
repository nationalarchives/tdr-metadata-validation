package uk.gov.nationalarchives.tdr.validation.schema.fields

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.nationalarchives.tdr.validation.schema.ValidationError
import uk.gov.nationalarchives.tdr.validation.schema.ValidationProcess.SCHEMA_BASE
import uk.gov.nationalarchives.tdr.validation.schema.helpers.TestHelper._

class DateLastModifiedSpec extends AnyWordSpecLike {

  "When validating against all schema we see " should {

    "success if a value (in the past) is provided for an open record" in {
      val openTestFileRow = openMetadataFileRow(dateLastModified = Some("2024-12-25"))
      validationErrors(openTestFileRow).size shouldBe 0
    }

    "success if a value is provided for a closed record" in {
      val closedTestFileRow = closedMetadataFileRow(dateLastModified = Some("2024-12-25"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "success if the column is missing (it is not required)" in {
      val openTestFileRow = openMetadataFileRow(dateLastModified = None)
      validationErrors(openTestFileRow).size shouldBe 0

      val closedTestFileRow = closedMetadataFileRow(dateLastModified = None)
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "error(s) is the cell is empty" in {
      val openTestFileRow = openMetadataFileRow(dateLastModified = Some(""))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "date_last_modified", "type") //We are unable to recognise this as a valid date format, please provide a date in the format yyyy-mm-dd
      )

      val closedTestFileRow = closedMetadataFileRow(dateLastModified = Some(""))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "date_last_modified", "type") //We are unable to recognise this as a valid date format, please provide a date in the format yyyy-mm-dd
      )
    }

    //TODO: is it OK that dlm can be in the future?
    "success if the value is in future" in {
      val openTestFileRow = openMetadataFileRow(dateLastModified = Some("3024-12-25"))
      validationErrors(openTestFileRow).size shouldBe 0

      val closedTestFileRow = closedMetadataFileRow(dateLastModified = Some("3024-12-25"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "error(s) if the value is invalid (not a date)" in {
      val openTestFileRow = openMetadataFileRow(dateLastModified = Some("xmas last year"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "date_last_modified", "format.date") //We are unable to recognise this as a valid date format, please provide a date in the format yyyy-mm-dd
      )
      val closedTestFileRow = closedMetadataFileRow(dateLastModified = Some("xmas last year"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "date_last_modified", "format.date") //We are unable to recognise this as a valid date format, please provide a date in the format yyyy-mm-dd
      )
    }

    "error(s) if the value is invalid (alt date format yyyy-dd-mm)" in {
      val openTestFileRow = openMetadataFileRow(dateLastModified = Some("2024-25-12"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "date_last_modified", "format.date") //We are unable to recognise this as a valid date format, please provide a date in the format yyyy-mm-dd
      )
      val closedTestFileRow = closedMetadataFileRow(dateLastModified = Some("2024-25-12"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "date_last_modified", "format.date") //We are unable to recognise this as a valid date format, please provide a date in the format yyyy-mm-dd
      )
    }

    "error(s) if the value is invalid (alt date format slash)" in {
      val openTestFileRow = openMetadataFileRow(dateLastModified = Some("25/12/2024"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "date_last_modified", "format.date") //We are unable to recognise this as a valid date format, please provide a date in the format yyyy-mm-dd
      )
      val closedTestFileRow = closedMetadataFileRow(dateLastModified = Some("25/12/2024"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "date_last_modified", "format.date") //We are unable to recognise this as a valid date format, please provide a date in the format yyyy-mm-dd
      )
    }

    "error(s) if the value is invalid (a boolean)" in {
      val openTestFileRow = openMetadataFileRow(dateLastModified = Some("Yes"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "date_last_modified", "format.date") //We are unable to recognise this as a valid date format, please provide a date in the format yyyy-mm-dd
      )
      val closedTestFileRow = closedMetadataFileRow(dateLastModified = Some("Yes"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "date_last_modified", "format.date") //We are unable to recognise this as a valid date format, please provide a date in the format yyyy-mm-dd
      )
    }
  }
}
