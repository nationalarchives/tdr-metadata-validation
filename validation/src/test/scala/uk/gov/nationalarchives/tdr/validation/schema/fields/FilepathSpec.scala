package uk.gov.nationalarchives.tdr.validation.schema.fields

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.nationalarchives.tdr.validation.schema.ValidationError
import uk.gov.nationalarchives.tdr.validation.schema.ValidationProcess.{SCHEMA_BASE, SCHEMA_REQUIRED}
import uk.gov.nationalarchives.tdr.validation.schema.helpers.TestHelper._

class FilepathSpec extends AnyWordSpecLike {

  "When validating against all schema we see" should {

    "success if a valid filepath is provided" in {
      val openTestFileRow = openMetadataFileRow(filePath = Some("folder/word.docx"))
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(filePath = Some("folder/word.docx"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "success if a short filepath is provided (one char minimum)" in {
      val openTestFileRow = openMetadataFileRow(filePath = Some("p"))
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(filePath = Some("p"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "success if a long filepath is provided (no max - this tests 8,001 char)" in {
      val openTestFileRow = openMetadataFileRow(filePath = Some(eightThousandCharString + "1"))
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(filePath = Some(eightThousandCharString + "1"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "error(s) if whole column is missing" in {
      val openTestFileRow = openMetadataFileRow(filePath = None)
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_REQUIRED, "file_path", "required")
      )
      val closedTestFileRow = closedMetadataFileRow(filePath = None)
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_REQUIRED, "file_path", "required")
      )
    }

    "error(s) if the cell itself is empty" in {
      val openTestFileRow = openMetadataFileRow(filePath = Some(""))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "file_path", "type") // Must not be empty
      )
      val closedTestFileRow = closedMetadataFileRow(filePath = Some(""))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "file_path", "type") // Must not be empty
      )
    }
  }
}
