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
        ValidationError(SCHEMA_BASE, "closure_type", "enum")  //Must be Open or Closed
      )
    }

    "error(s) if the value is closed (no capital C for Closed) for a closed record" in {
      val closedTestFileRow = closedMetadataFileRow(closureType = Some("closed"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "closure_type", "enum")  //Must be Open or Closed
      )
    }

    "error(s) if the value is missing for an open record" in {
      val openTestFileRow = openMetadataFileRow(closureType = Some(""))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "closure_type", "type"), //Must be either Open or Closed  //TODO: duplicated msgs?
        ValidationError(SCHEMA_BASE, "closure_type", "enum")  //Must be Open or Closed
      )
    }

    "error(s) if the value is missing for a closed record" in {
      val closedTestFileRow = closedMetadataFileRow(closureType = Some(""))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "closure_type", "type"), //Must be either Open or Closed  //TODO: duplicated msgs?
        ValidationError(SCHEMA_BASE, "closure_type", "enum")  //Must be Open or Closed
      )
    }

    "error(s) if the value is invalid (neither Open nor Closed) for an open record" in {
      val openTestFileRow = openMetadataFileRow(closureType = Some("neither Open nor Closed"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "closure_type", "enum")  //Must be Open or Closed
      )
    }

    "error(s) if the value is invalid (neither Open nor Closed) for a closed record" in {
      val closedTestFileRow = closedMetadataFileRow(closureType = Some("neither Open nor Closed"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "closure_type", "enum")  //Must be Open or Closed
      )
    }
  }
}

