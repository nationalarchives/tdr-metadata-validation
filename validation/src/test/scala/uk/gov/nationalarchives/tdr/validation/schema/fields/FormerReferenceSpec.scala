package uk.gov.nationalarchives.tdr.validation.schema.fields

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.nationalarchives.tdr.validation.schema.ValidationError
import uk.gov.nationalarchives.tdr.validation.schema.ValidationProcess.SCHEMA_BASE
import uk.gov.nationalarchives.tdr.validation.schema.helpers.TestHelper._

class FormerReferenceSpec extends AnyWordSpecLike {

  "When validating against all schema we see" should {

    "success if a valid former reference is provided" in {
      val openTestFileRow = openMetadataFileRow(formerReference = Some("a former reference is added"))
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(formerReference = Some("a former reference is added"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "succeeds if the cell itself is empty" in {
      val openTestFileRow = openMetadataFileRow(formerReference = Some(""))
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(formerReference = Some(""))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "succeeds if whole column is missing" in {
      val openTestFileRow = openMetadataFileRow(formerReference = None)
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(formerReference = None)
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "succeeds if a single char former reference is provided " in {
      val openTestFileRow = openMetadataFileRow(formerReference = Some("a"))
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(formerReference = Some("a"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "succeeds if a 255 char former reference is provided " in {
      val openTestFileRow = openMetadataFileRow(formerReference = Some(twoHundredAndFiftyFiveCharString))
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(formerReference = Some(twoHundredAndFiftyFiveCharString))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "errors if a long former reference is provided (this tests 256 char)" in {
      val openTestFileRow = openMetadataFileRow(formerReference = Some(twoHundredAndFiftyFiveCharString + "1"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "former_reference_department", "maxLength") // More than 255 characters, this field has a maximum length of 255 characters
      )
      val closedTestFileRow = closedMetadataFileRow(formerReference = Some(twoHundredAndFiftyFiveCharString + "1"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "former_reference_department", "maxLength") // More than 255 characters, this field has a maximum length of 255 characters
      )
    }
  }
}
