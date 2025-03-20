package uk.gov.nationalarchives.tdr.validation.schema.fields

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.nationalarchives.tdr.validation.schema.helpers.TestHelper._
import uk.gov.nationalarchives.tdr.validation.schema.ValidationError
import uk.gov.nationalarchives.tdr.validation.schema.ValidationProcess.SCHEMA_BASE

class FilenameSpec extends AnyWordSpecLike {

  "When validating against all schema we see" should {

    "success if whole column is missing" in {
      val openTestFileRow = openMetadataFileRow(fileName = None)
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(fileName = None)
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "success if a valid filename is provided" in {
      val openTestFileRow = openMetadataFileRow(fileName = Some("word.docx"))
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(fileName = Some("word.docx"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "success if a short filename is provided (one char minimum)" in {
      val openTestFileRow = openMetadataFileRow(fileName = Some("a"))
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(fileName = Some("a"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "success if a long filename is provided (no max - this tests 8,001 char)" in {
      val openTestFileRow = openMetadataFileRow(fileName = Some(eightThousandCharString + "1"))
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(fileName = Some(eightThousandCharString + "1"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "error(s) if the cell itself is empty" in {
      val openTestFileRow = openMetadataFileRow(fileName = Some(""))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "file_name", "type") // Must not be empty
      )
      val closedTestFileRow = closedMetadataFileRow(fileName = Some(""))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "file_name", "type") // Must not be empty
      )
    }
  }
}
