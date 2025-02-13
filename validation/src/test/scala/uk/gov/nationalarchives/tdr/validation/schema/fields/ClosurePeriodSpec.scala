package uk.gov.nationalarchives.tdr.validation.schema.fields

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.nationalarchives.tdr.validation.schema.helpers.TestHelper._
import uk.gov.nationalarchives.tdr.validation.schema.ValidationError
import uk.gov.nationalarchives.tdr.validation.schema.ValidationProcess.{SCHEMA_BASE, SCHEMA_CLOSURE_CLOSED, SCHEMA_CLOSURE_OPEN, SCHEMA_REQUIRED}

class ClosurePeriodSpec extends AnyWordSpecLike {

  "When validating against all schema we see " should {

    "success if the value is 1 (the min allowed) for a closed record" in {
      val closedTestFileRow = closedMetadataFileRow(closurePeriod = Some("99"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "success if the value is 150 (the max allowed) for a closed record" in {
      val closedTestFileRow = closedMetadataFileRow(closurePeriod = Some("150"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "success if the value is missing for an open record" in {
      val openTestFileRow = openMetadataFileRow(closurePeriod = Some(""))
      validationErrors(openTestFileRow).size shouldBe 0
    }

    "error(s) if the column is missing" in {
      val openTestFileRow = openMetadataFileRow(closurePeriod = None)
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_REQUIRED, "closure_period", "required")
      )
      val closedTestFileRow = closedMetadataFileRow(closurePeriod = None)
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_REQUIRED, "closure_period", "required")
      )
    }

    "error(s) if the value is less than one" in {
      val closedTestFileRow = closedMetadataFileRow(closurePeriod = Some("0"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "closure_period", "minimum")   //Must be a number between 1 and 150
      )
    }

    "error(s) if the value is negative" in {
      val closedTestFileRow = closedMetadataFileRow(closurePeriod = Some("-99"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "closure_period", "minimum")   //Must be a number between 1 and 150
      )
    }

    "error(s) if the value is greater than 150" in {
      val closedTestFileRow = closedMetadataFileRow(closurePeriod = Some("151"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "closure_period", "maximum")   //Must be a number between 1 and 150
      )
    }

    "error(s) if the value is a string" in {
      val closedTestFileRow = closedMetadataFileRow(closurePeriod = Some("one hundred as a string"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "closure_period", "unionType"),      //Must be a number between 1 and 150
        ValidationError(SCHEMA_CLOSURE_CLOSED, "closure_period", "type")  //Must be provided for a closed record  //TODO: review double msgs (see below)
      )
    }

    "error(s) if the value is a boolean" in {
      val closedTestFileRow = closedMetadataFileRow(closurePeriod = Some("Yes"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "closure_period", "unionType"),      //Must be a number between 1 and 150
        ValidationError(SCHEMA_CLOSURE_CLOSED, "closure_period", "type")  //Must be provided for a closed record  //TODO: review double msgs (see below)
      )
    }

    "error(s) if the value is missing for a closed record" in {
      val closedTestFileRow = closedMetadataFileRow(closurePeriod = Some(""))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_CLOSED, "closure_period", "type")  //Must be provided for a closed record
      )
    }

    "error(s) if the value is present for an open record" in {
      val openTestFileRow = openMetadataFileRow(closurePeriod = Some("99"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_OPEN, "closure_period", "type")  //Must be empty for an open record
      )
    }

    "error(s) if the value is present (and also invalid) for an open record" in {
      val openTestFileRow = openMetadataFileRow(closurePeriod = Some("999"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_OPEN, "closure_period", "type"),  //Must be empty for an open record
        ValidationError(SCHEMA_BASE, "closure_period", "maximum")        //Must be a number between 1 and 150 //TODO: conflicting msgs a bit?
      )
    }
  }
}

