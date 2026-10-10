package uk.gov.nationalarchives.tdr.validation.schema.fields

import org.scalatest.Inspectors
import org.scalatest.matchers.should.Matchers._
import org.scalatest.prop.TableFor1
import org.scalatest.prop.Tables.Table
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.nationalarchives.tdr.validation.schema.ValidationError
import uk.gov.nationalarchives.tdr.validation.schema.ValidationProcess.SCHEMA_BASE
import uk.gov.nationalarchives.tdr.validation.schema.helpers.TestHelper._

class CataloguePlacementSpec extends AnyWordSpecLike {

  "When validating against all schema we see" should {

    "success if a valid catalogue placement is provided" in {
      val openTestFileRow = openMetadataFileRow(cataloguePlacement = Some("YHC/CL/JO/3/21/ZAB4D"))
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(cataloguePlacement = Some("YHC/CL/JO/3/21/ZAB4D"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "succeeds if the cell itself is empty" in {
      val openTestFileRow = openMetadataFileRow(cataloguePlacement = Some(""))
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(cataloguePlacement = Some(""))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "succeeds if whole column is missing" in {
      val openTestFileRow = openMetadataFileRow(cataloguePlacement = None)
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(cataloguePlacement = None)
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    val valueTable: TableFor1[String] = Table(
      "Value",
      "YH!/C",
      "YHC",
      "YHC/",
      "YHCGAH/GB",
      "1YH/GB",
      "YH1/GB",
      "11/GB",
      "/YHC",
      "!/YHC"
    )
    Inspectors.forAll(valueTable)(value => {
      s"errors if catalogue placement is not in the right format - $value" in {
        val test1 = openMetadataFileRow(cataloguePlacement = Some(value))
        validationErrors(test1).size shouldBe 1
      }
    })

    "succeeds if a 50 char catalogue placement is provided " in {
      val openTestFileRow = openMetadataFileRow(cataloguePlacement = Some("YHC/CL/JO1" + "A" * 40))
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(cataloguePlacement = Some("YHC/CL/JO1" + "A" * 40))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "errors if a long catalogue placement is provided (this tests 51 char)" in {
      val openTestFileRow = openMetadataFileRow(cataloguePlacement = Some("YHC/CL/JO1" + "A" * 41))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "catalogue_placement", "maxLength") // More than 50 characters, this field has a maximum length of 50 characters
      )
      val closedTestFileRow = closedMetadataFileRow(cataloguePlacement = Some("YHC/CL/JO1" + "A" * 41))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "catalogue_placement", "maxLength") // More than 50 characters, this field has a maximum length of 50 characters
      )
    }
  }
}
