package uk.gov.nationalarchives.tdr.validation.schema.fields

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.nationalarchives.tdr.validation.schema.ValidationError
import uk.gov.nationalarchives.tdr.validation.schema.ValidationProcess.SCHEMA_BASE
import uk.gov.nationalarchives.tdr.validation.schema.helpers.TestHelper._

class FormerFilepathSpec extends AnyWordSpecLike {

  "When validating against all schema we see" should {

    "success if a valid former filepath is provided" in {
      val openTestFileRow = openMetadataFileRow(formerFilePath = Some("a former filepath is added"))
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(formerFilePath = Some("a former filepath is added"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "succeeds if the cell itself is empty" in {
      val openTestFileRow = openMetadataFileRow(formerFilePath = Some(""))
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(formerFilePath = Some(""))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "succeeds if whole column is missing" in {
      val openTestFileRow = openMetadataFileRow(formerFilePath = None)
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(formerFilePath = None)
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "succeeds if a single char former filepath is provided " in {
      val openTestFileRow = openMetadataFileRow(formerFilePath = Some("a"))
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(formerFilePath = Some("a"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "succeeds if a 1000 char former filepath is provided " in {
      val openTestFileRow = openMetadataFileRow(formerFilePath = Some(thousandCharString))
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(formerFilePath = Some(thousandCharString))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "errors if a long former filepath is provided (this tests 1001 char)" in {
      val openTestFileRow = openMetadataFileRow(formerFilePath = Some(thousandCharString + "1"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "former_filepath_department", "maxLength") // More than 1000 characters, this field has a maximum length of 1000 characters
      )
      val closedTestFileRow = closedMetadataFileRow(formerFilePath = Some(thousandCharString + "1"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "former_filepath_department", "maxLength") // More than 1000 characters, this field has a maximum length of 1000 characters
      )
    }
  }
}
