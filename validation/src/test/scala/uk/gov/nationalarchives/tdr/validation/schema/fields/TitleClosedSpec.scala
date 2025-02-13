package uk.gov.nationalarchives.tdr.validation.schema.fields

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.nationalarchives.tdr.validation.schema.helpers.TestHelper._
import uk.gov.nationalarchives.tdr.validation.schema.ValidationError
import uk.gov.nationalarchives.tdr.validation.schema.ValidationProcess.{SCHEMA_CLOSURE_CLOSED, SCHEMA_CLOSURE_OPEN, SCHEMA_REQUIRED, SCHEMA_BASE}

class TitleClosedSpec extends AnyWordSpecLike {

  "When validating against all schema we see " should {

    "success if the value is No for an open record" in {
      val openTestFileRow = openMetadataFileRow(titleClosed = Some("No"))
      validationErrors(openTestFileRow).size shouldBe 0
    }

    "success if the value is no for an open record (it does not need a capital N)" in {
      val openTestFileRow = openMetadataFileRow(titleClosed = Some("no"))
      validationErrors(openTestFileRow).size shouldBe 0
    }

    "success if the value is No for a closed record and no alternate title is provided" in {
      val closedTestFileRow = closedMetadataFileRow(titleClosed = Some("No"), titleAlternative = Some(""))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "success if the value is Yes for a closed record and an alternate title is provided" in {
      val closedTestFileRow = closedMetadataFileRow(titleClosed = Some("Yes"), titleAlternative = Some("alt title"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "error(s) if the column is missing" in {
      val openTestFileRow = openMetadataFileRow(titleClosed = None)
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_REQUIRED, "title_closed", "required")
      )
      val closedTestFileRow = closedMetadataFileRow(titleClosed = None)
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_REQUIRED, "title_closed", "required")
      )
    }

    "error(s) if the value is yes for an open record" in {
      val openTestFileRow = openMetadataFileRow(titleClosed = Some("Yes"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_OPEN, "title_closed", "const") // Must be No for an open record
      )
    }

    "error(s) if the value is invalid for an open record" in {
      val openTestFileRow = openMetadataFileRow(titleClosed = Some("neither Yes nor No"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "title_closed", "type"), // Must be Yes or No
        ValidationError(SCHEMA_CLOSURE_OPEN, "title_closed", "const") // Must be No for an open record
      )
    }

    "error(s) if the value is missing for an open record" in {
      val openTestFileRow = openMetadataFileRow(titleClosed = Some(""))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "title_closed", "type"), // Must be Yes or No
        ValidationError(SCHEMA_CLOSURE_OPEN, "title_closed", "const") // Must be No for an open record
      )
    }

    "error(s) if the value is invalid for a closed record" in {
      val closedTestFileRow = closedMetadataFileRow(titleClosed = Some("neither Yes nor No"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "title_closed", "type"), // Must be Yes or No
        ValidationError(SCHEMA_CLOSURE_CLOSED, "title_closed", "type") // Must be provided for a closed record //TODO: it is wrong not missing (see next test)
      )
    }

    "error(s) if the value is missing for a closed record" in {
      val closedTestFileRow = closedMetadataFileRow(titleClosed = Some(""))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "title_closed", "type"), // Must be Yes or No
        ValidationError(SCHEMA_CLOSURE_CLOSED, "title_closed", "type") // Must be provided for a closed record
      )
    }
  }
}
