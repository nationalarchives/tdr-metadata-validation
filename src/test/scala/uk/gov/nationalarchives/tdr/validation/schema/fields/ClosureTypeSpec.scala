package uk.gov.nationalarchives.tdr.validation.schema.fields

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.nationalarchives.tdr.validation.schema.helpers.TestHelper._
import uk.gov.nationalarchives.tdr.validation.schema.ValidationError
import uk.gov.nationalarchives.tdr.validation.schema.ValidationProcess.{SCHEMA_BASE, SCHEMA_REQUIRED}

class ClosureTypeSpec extends AnyWordSpecLike {

  "When validating against all schema we see " should {

    "success if the value is Open for an open record" in {
      val openTestFileRow = openMetadataFileRow(closureType = Some("Open"))
      validationErrors(openTestFileRow).size shouldBe 0
    }

    "success if the value is Closed for a closed record" in {
      val closedTestFileRow = closedMetadataFileRow(closureType = Some("Closed"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "success if the value is 'Retained for security' for a retained record" in {
      val retainedTestFileRow = retainedMetadataFileRow(closureType = Some("Retained for security"))
      validationErrors(retainedTestFileRow).size shouldBe 0
    }

    "error(s) if the column is missing" in {
      val openTestFileRow = openMetadataFileRow(closureType = None)
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_REQUIRED, "closure_type", "required")
      )
      val closedTestFileRow = closedMetadataFileRow(closureType = None)
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_REQUIRED, "closure_type", "required")
      )
    }

    "error(s) if the value is open (no capital O for Open) for an open record" in {
      val openTestFileRow = openMetadataFileRow(closureType = Some("open"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "closure_type", "enum") // Must be Open, Closed or 'Retained for security'
      )
    }

    "error(s) if the value is closed (no capital C for Closed) for a closed record" in {
      val closedTestFileRow = closedMetadataFileRow(closureType = Some("closed"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "closure_type", "enum") // Must be Open, Closed or 'Retained for security'
      )
    }

    "error(s) if the value is missing for an open record" in {
      val openTestFileRow = openMetadataFileRow(closureType = Some(""))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "closure_type", "type"), // Must be either Open or Closed
        ValidationError(SCHEMA_BASE, "closure_type", "enum") // Must be Open, Closed or 'Retained for security'
      )
    }

    "error(s) if the value is missing for a closed record" in {
      val closedTestFileRow = closedMetadataFileRow(closureType = Some(""))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "closure_type", "type"), // Must be either Open or Closed
        ValidationError(SCHEMA_BASE, "closure_type", "enum") // Must be Open, Closed or 'Retained for security'
      )
    }

    "error(s) if the value is invalid (neither Open or Closed or Retained for security) for an open record" in {
      val openTestFileRow = openMetadataFileRow(closureType = Some("Invalid"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "closure_type", "enum") // Must be Open, Closed or 'Retained for security'
      )
    }

    "error(s) if the value is invalid (neither Open or Closed or Retained for security) for a closed record" in {
      val closedTestFileRow = closedMetadataFileRow(closureType = Some("Invalid"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "closure_type", "enum") // Must be Open, Closed or 'Retained for security'
      )
    }
  }
}
