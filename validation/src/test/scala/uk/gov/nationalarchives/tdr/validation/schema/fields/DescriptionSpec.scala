package uk.gov.nationalarchives.tdr.validation.schema.fields

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.nationalarchives.tdr.validation.schema.ValidationError
import uk.gov.nationalarchives.tdr.validation.schema.ValidationProcess.{SCHEMA_BASE, SCHEMA_REQUIRED}
import uk.gov.nationalarchives.tdr.validation.schema.helpers.TestHelper._

class DescriptionSpec extends AnyWordSpecLike {

  "When validating against all schema we see" should {

    "success if a valid description is provided" in {
      val openTestFileRow = openMetadataFileRow(description = Some("a description is added"))
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(description = Some("a description is added"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "success if a short description is provided (one char minimum)" in {
      val openTestFileRow = openMetadataFileRow(description = Some("p"))
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(description = Some("p"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "succeeds if the cell itself is empty" in {
      val openTestFileRow = openMetadataFileRow(description = Some(""))
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(description = Some(""))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "succeeds if a 8,000 char description is provided " in {
      val openTestFileRow = openMetadataFileRow(description = Some(eightThousandCharString))
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(description = Some(eightThousandCharString))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "succeeds with a carriage return in  the description" in {
      val openTestFileRow = openMetadataFileRow(description = Some("a description \n including a carriage return"))
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(description = Some("a description \n including a carriage return"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "error(s) if whole column is missing" in {
      val openTestFileRow = openMetadataFileRow(description = None)
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_REQUIRED, "description", "required")
      )
      val closedTestFileRow = closedMetadataFileRow(description = None)
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_REQUIRED, "description", "required")
      )
    }

    "errors if a long description is provided (no max - this tests 8,001 char)" in {
      val openTestFileRow = openMetadataFileRow(description = Some(eightThousandCharString + "1"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "description", "maxLength") // More than 8000 characters, this field has a maximum length of 8000 characters
      )
      val closedTestFileRow = closedMetadataFileRow(description = Some(eightThousandCharString + "1"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "description", "maxLength") // More than 8000 characters, this field has a maximum length of 8000 characters
      )
    }
  }
}
