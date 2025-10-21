package uk.gov.nationalarchives.tdr.validation.schema.fields

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.nationalarchives.tdr.validation.schema.ValidationError
import uk.gov.nationalarchives.tdr.validation.schema.ValidationProcess.SCHEMA_BASE
import uk.gov.nationalarchives.tdr.validation.schema.helpers.TestHelper._

class EvidenceProvidedBySpec extends AnyWordSpecLike {

  "When validating against schema we see" should {

    "success if a valid former reference is provided" in {
      val openTestFileRow = openMetadataFileRow(evidenceProvidedBy = Some("evidence provided by is added"))
      validationErrors(openTestFileRow).size shouldBe 0
    }

    "succeeds if the cell itself is empty" in {
      val openTestFileRow = openMetadataFileRow(evidenceProvidedBy = Some(""))
      validationErrors(openTestFileRow).size shouldBe 0
    }

    "succeeds if whole column is missing" in {
      val openTestFileRow = openMetadataFileRow(evidenceProvidedBy = None)
      validationErrors(openTestFileRow).size shouldBe 0
    }

    "succeeds if a single char evidence provided by is provided " in {
      val openTestFileRow = openMetadataFileRow(evidenceProvidedBy = Some("a"))
      validationErrors(openTestFileRow).size shouldBe 0
    }

    "succeeds if a 255 char evidence provided by is provided " in {
      val openTestFileRow = openMetadataFileRow(evidenceProvidedBy = Some(twoHundredAndFiftyFiveCharString))
      validationErrors(openTestFileRow).size shouldBe 0
    }

    "errors if a long evidence provided by is provided (this tests 256 char)" in {
      val openTestFileRow = openMetadataFileRow(evidenceProvidedBy = Some(twoHundredAndFiftyFiveCharString + "1"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "evidence_provided_by", "maxLength") // More than 255 characters, this field has a maximum length of 255 characters
      )
    }
    "errors if evidence provided by contains \n" in {
      val openTestFileRow = openMetadataFileRow(evidenceProvidedBy = Some("evidence\nprovided by"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "evidence_provided_by", "pattern")
      )
    }
  }
}