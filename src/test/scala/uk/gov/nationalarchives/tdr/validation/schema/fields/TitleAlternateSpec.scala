package uk.gov.nationalarchives.tdr.validation.schema.fields

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.nationalarchives.tdr.validation.schema.helpers.TestHelper._
import uk.gov.nationalarchives.tdr.validation.schema.ValidationError
import uk.gov.nationalarchives.tdr.validation.schema.ValidationProcess.{SCHEMA_BASE, SCHEMA_CLOSURE_CLOSED, SCHEMA_CLOSURE_OPEN, SCHEMA_REQUIRED}

class TitleAlternateSpec extends AnyWordSpecLike {

  "When validating against all schema we see " should {

    "success if the value is not provided for an open record" in {
      val openTestFileRow = openMetadataFileRow(titleAlternative = Some(""))
      validationErrors(openTestFileRow).size shouldBe 0
    }

    "success if the value is provided for closed record with a closed title" in {
      val closedTestFileRow = closedMetadataFileRow(titleClosed = Some("Yes"), titleAlternative = Some("alt title"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "success if no value is provided for closed record with a open title" in {
      val closedTestFileRow = closedMetadataFileRow(titleClosed = Some("No"), titleAlternative = Some(""))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "successful treatment of false as a string" in {
      val closedTestFileRow = closedMetadataFileRow(titleClosed = Some("Yes"), titleAlternative = Some("false"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "successful treatment of 123 as a string" in {
      val closedTestFileRow = closedMetadataFileRow(titleClosed = Some("Yes"), titleAlternative = Some("123"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "error(s) if the column is missing" in {
      val openTestFileRow = openMetadataFileRow(titleAlternative = None)
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_REQUIRED, "title_alternate", "required")
      )
      val closedTestFileRow = closedMetadataFileRow(titleAlternative = None)
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_REQUIRED, "title_alternate", "required")
      )
    }

    "error(s) if the value is provided for an open record" in {
      val openTestFileRow = openMetadataFileRow(titleAlternative = Some("alt title"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_OPEN, "title_alternate", "type") // Must not be empty if title is closed
      )
    }

    "error(s) if the value is provided for closed record with a open title" in {
      val closedTestFileRow = closedMetadataFileRow(titleClosed = Some("No"), titleAlternative = Some("alt title"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_CLOSED, "title_closed", "const") // Must be Yes if an alternate is provided
      )
    }

    "error(s) if no value is provided for closed record with a closed title" in {
      val closedTestFileRow = closedMetadataFileRow(titleClosed = Some("Yes"), titleAlternative = Some(""))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_CLOSED, "title_alternate", "type") // Must not be empty if title is closed
      )
    }

    "error(s) if a value that is too long is provided" in {
      val closedTestFileRow = closedMetadataFileRow(
        titleClosed = Some("Yes"),
        titleAlternative = Some(eightThousandCharString + "1")
      )
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "title_alternate", "maxLength") // More than 8000 characters, this field has a maximum length of 8000 characters
      )
    }
  }
}
