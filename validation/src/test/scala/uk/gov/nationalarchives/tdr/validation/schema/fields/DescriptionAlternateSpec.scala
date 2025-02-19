package uk.gov.nationalarchives.tdr.validation.schema.fields

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.nationalarchives.tdr.validation.schema.helpers.TestHelper._
import uk.gov.nationalarchives.tdr.validation.schema.ValidationError
import uk.gov.nationalarchives.tdr.validation.schema.ValidationProcess.{SCHEMA_CLOSURE_CLOSED, SCHEMA_CLOSURE_OPEN, SCHEMA_REQUIRED, SCHEMA_BASE}

class DescriptionAlternateSpec extends AnyWordSpecLike {

  "When validating against all schema we see " should {

    "success if the value is not provided for an open record" in {
      val openTestFileRow = openMetadataFileRow(descriptionAlternative = Some(""))
      validationErrors(openTestFileRow).size shouldBe 0
    }

    "success if the value is provided for closed record with a closed description" in {
      val closedTestFileRow = closedMetadataFileRow(descriptionClosed = Some("Yes"), descriptionAlternative = Some("alt descr"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "success if no value is provided for closed record with a open description" in {
      val closedTestFileRow = closedMetadataFileRow(descriptionClosed = Some("No"), descriptionAlternative = Some(""))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "error(s) if the column is missing" in {
      val openTestFileRow = openMetadataFileRow(descriptionAlternative = None)
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_REQUIRED, "description_alternate", "required")
      )
      val closedTestFileRow = closedMetadataFileRow(descriptionAlternative = None)
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_REQUIRED, "description_alternate", "required")
      )
    }

    "error(s) if the value is provided for an open record" in {
      val openTestFileRow = openMetadataFileRow(descriptionAlternative = Some("alt descr"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_OPEN, "description_alternate", "type") // Must be empty for an open record
      )
    }

    "error(s) if the value is provided for closed record with a open description" in {
      val closedTestFileRow = closedMetadataFileRow(descriptionClosed = Some("No"), descriptionAlternative = Some("alt descr"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_CLOSED, "description_closed", "const") // Must be Yes if an alternate is provided
      )
    }

    "error(s) if no value is provided for closed record with a closed description" in {
      val closedTestFileRow = closedMetadataFileRow(descriptionClosed = Some("Yes"), descriptionAlternative = Some(""))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_CLOSED, "description_alternate", "type") // Must not be empty if description is closed
      )
    }

    "error(s) if a value that is too long is provided" in {
      val closedTestFileRow = closedMetadataFileRow(
        descriptionClosed = Some("Yes"),
        descriptionAlternative = Some(eightThousandCharString +"Z")
      )
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "description_alternate", "maxLength") // More than 8000 characters, this field has a maximum length of 8000 characters
      )
    }
  }
}
