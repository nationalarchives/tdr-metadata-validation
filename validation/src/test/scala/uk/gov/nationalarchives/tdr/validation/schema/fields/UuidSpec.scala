package uk.gov.nationalarchives.tdr.validation.schema.fields

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.nationalarchives.tdr.validation.schema.ValidationError
import uk.gov.nationalarchives.tdr.validation.schema.ValidationProcess.SCHEMA_BASE
import uk.gov.nationalarchives.tdr.validation.schema.helpers.TestHelper._

class UuidSpec extends AnyWordSpecLike {

  "When validating against all schema we see" should {

    "success if whole column is missing" in {
      val openTestFileRow = openMetadataFileRow(uuid = None)
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(uuid = None)
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "success if a valid uuid is provided" in {
      val openTestFileRow = openMetadataFileRow(uuid = Some("5c76eaf6-df2a-42f2-b355-0ad01f7bcfdd"))
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(uuid = Some("5c76eaf6-df2a-42f2-b355-0ad01f7bcfdd"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "error(s) if the cell itself is empty" in {
      val openTestFileRow = openMetadataFileRow(uuid = Some(""))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "UUID", "type") // TODO: needs a msg
      )
      val closedTestFileRow = closedMetadataFileRow(uuid = Some(""))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "UUID", "type") // TODO: needs a msg
      )
    }

    "error(s) if an invalid uuid is provided" in {
      val openTestFileRow = openMetadataFileRow(uuid = Some("5c76eaf6-df2a-42f2-b355-0ad01f7bcfdd-ZZZ"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "UUID", "format.uuid") // Invalid format
      )
      val closedTestFileRow = closedMetadataFileRow(uuid = Some("5c76eaf6-df2a-42f2-b355-0ad01f7bcfdd-ZZZ"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "UUID", "format.uuid") // Invalid format
      )
    }
  }
}
