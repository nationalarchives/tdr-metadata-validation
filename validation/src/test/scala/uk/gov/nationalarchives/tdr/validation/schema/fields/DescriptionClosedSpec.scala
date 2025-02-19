package uk.gov.nationalarchives.tdr.validation.schema.fields

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.nationalarchives.tdr.validation.schema.helpers.TestHelper._
import uk.gov.nationalarchives.tdr.validation.schema.ValidationError
import uk.gov.nationalarchives.tdr.validation.schema.ValidationProcess.{SCHEMA_BASE, SCHEMA_CLOSURE_CLOSED, SCHEMA_CLOSURE_OPEN, SCHEMA_REQUIRED}

class DescriptionClosedSpec extends AnyWordSpecLike {

  "When validating against all schema we see " should {

    "success if the value is No for an open record" in {
      val openTestFileRow = openMetadataFileRow(descriptionClosed = Some("No"))
      validationErrors(openTestFileRow).size shouldBe 0
    }

    "success if the value is no for an open record (it does not need a capital N)" in {
      val openTestFileRow = openMetadataFileRow(descriptionClosed = Some("no"))
      validationErrors(openTestFileRow).size shouldBe 0
    }

    "success if the value is No for a closed record and no alternate description is provided" in {
      val closedTestFileRow = closedMetadataFileRow(descriptionClosed = Some("No"), descriptionAlternative = Some(""))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "success if the value is Yes for a closed record and an alternate description is provided" in {
      val closedTestFileRow = closedMetadataFileRow(descriptionClosed = Some("Yes"), descriptionAlternative = Some("alt descr"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "success if the value is yes for a closed record (it does not need a capital Y)" in {
      val closedTestFileRow = closedMetadataFileRow(descriptionClosed = Some("yes"), descriptionAlternative = Some("alt descr"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "successful treatment of false as a string" in {
      val closedTestFileRow = closedMetadataFileRow(descriptionClosed = Some("Yes"), descriptionAlternative = Some("false"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "successful treatment of 123 as a string" in {
      val closedTestFileRow = closedMetadataFileRow(descriptionClosed = Some("Yes"), descriptionAlternative = Some("123"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "error(s) if the column is missing" in {
      val openTestFileRow = openMetadataFileRow(descriptionClosed = None)
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_REQUIRED, "description_closed", "required")
      )
      val closedTestFileRow = closedMetadataFileRow(descriptionClosed = None)
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_REQUIRED, "description_closed", "required")
      )
    }

    "error(s) if the value is yes for an open record" in {
      val openTestFileRow = openMetadataFileRow(descriptionClosed = Some("Yes"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_OPEN, "description_closed", "const") // Must be No for an open record
      )
    }

    "error(s) if the value is invalid for an open record" in {
      val openTestFileRow = openMetadataFileRow(descriptionClosed = Some("neither Yes nor No"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "description_closed", "type"), // Must be Yes or No
        ValidationError(SCHEMA_CLOSURE_OPEN, "description_closed", "const") // Must be No for an open record
      )
    }

    "error(s) if the value is missing for an open record" in {
      val openTestFileRow = openMetadataFileRow(descriptionClosed = Some(""))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "description_closed", "type"), // Must be Yes or No
        ValidationError(SCHEMA_CLOSURE_OPEN, "description_closed", "const") // Must be No for an open record
      )
    }

    "error(s) if the value is invalid for a closed record" in {
      val closedTestFileRow = closedMetadataFileRow(descriptionClosed = Some("neither Yes nor No"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "description_closed", "type"), // Must be Yes or No
        ValidationError(SCHEMA_CLOSURE_CLOSED, "description_closed", "type") // Must be provided for a closed record //TODO: it is wrong not missing (see next test)
      )
    }

    "error(s) if the value is missing for a closed record" in {
      val closedTestFileRow = closedMetadataFileRow(descriptionClosed = Some(""))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "description_closed", "type"), // Must be Yes or No
        ValidationError(SCHEMA_CLOSURE_CLOSED, "description_closed", "type") // Must be provided for a closed record
      )
    }
  }
}
