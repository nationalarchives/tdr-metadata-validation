package uk.gov.nationalarchives.tdr.validation.schema.fields

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.nationalarchives.tdr.validation.schema.ValidationError
import uk.gov.nationalarchives.tdr.validation.schema.ValidationProcess.SCHEMA_BASE
import uk.gov.nationalarchives.tdr.validation.schema.helpers.TestHelper._

class FileNameTranslationSpec extends AnyWordSpecLike {

  "When validating against all schema we see" should {

    "success if a valid file name translation is provided" in {
      val openTestFileRow = openMetadataFileRow(translatedTitleOfRecord = Some("a file name translation is added"))
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(translatedTitleOfRecord = Some("a file name translation is added"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "success if a short file name translation is provided (one char minimum)" in {
      val openTestFileRow = openMetadataFileRow(translatedTitleOfRecord = Some("p"))
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(translatedTitleOfRecord = Some("p"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "succeeds if the cell itself is empty" in {
      val openTestFileRow = openMetadataFileRow(translatedTitleOfRecord = Some(""))
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(translatedTitleOfRecord = Some(""))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "succeeds if a 255 char file name translation is provided " in {
      val openTestFileRow = openMetadataFileRow(translatedTitleOfRecord = Some(twoHundredAndFiftyFiveCharString))
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(translatedTitleOfRecord = Some(twoHundredAndFiftyFiveCharString))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "succeeds with a carriage return in the file name translation" in {
      val openTestFileRow = openMetadataFileRow(translatedTitleOfRecord = Some("a file name translation \n including a carriage return"))
      validationErrors(openTestFileRow).size shouldBe 1
      val closedTestFileRow = closedMetadataFileRow(translatedTitleOfRecord = Some("a file name translation \n including a carriage return"))
      validationErrors(closedTestFileRow).size shouldBe 1
    }

    "errors if a long file name translation is provided (no max - this tests 8,001 char)" in {
      val openTestFileRow = openMetadataFileRow(translatedTitleOfRecord = Some(eightThousandCharString + "1"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "file_name_translation", "maxLength") // More than 8000 characters, this field has a maximum length of 8000 characters
      )
      val closedTestFileRow = closedMetadataFileRow(translatedTitleOfRecord = Some(eightThousandCharString + "1"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "file_name_translation", "maxLength") // More than 8000 characters, this field has a maximum length of 8000 characters
      )
    }
  }
}
