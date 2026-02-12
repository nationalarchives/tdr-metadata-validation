package uk.gov.nationalarchives.tdr.validation.schema.fields

import cats.implicits.catsSyntaxOptionId
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.nationalarchives.tdr.schemautils.ConfigUtils.ARRAY_SPLIT_CHAR
import uk.gov.nationalarchives.tdr.validation.schema.ValidationError
import uk.gov.nationalarchives.tdr.validation.schema.ValidationProcess.{SCHEMA_BASE, SCHEMA_REQUIRED}
import uk.gov.nationalarchives.tdr.validation.schema.helpers.TestHelper._

class RightsCopyrightSpec extends AnyWordSpecLike {

  "When validating against all schema we see " should {

    "success if a single value is valid" in {
      val fileRow = openMetadataFileRow(rightsCopyRight = "UK Parliament".some)
      validationErrors(fileRow).size shouldBe 0
    }

    "success if a 'Unknown' value is provided" in {
      val fileRow = openMetadataFileRow(rightsCopyRight = "Unknown".some)
      validationErrors(fileRow).size shouldBe 0
    }

    "success if all possible valid values (not 'Unknown') are provided" in {
      val fileRow = openMetadataFileRow(rightsCopyRight =
        s"Crown copyright${ARRAY_SPLIT_CHAR}Third party${ARRAY_SPLIT_CHAR}Crown${ARRAY_SPLIT_CHAR}UK Parliament${ARRAY_SPLIT_CHAR}Open Parliament Licence".some
      )
      validationErrors(fileRow).size shouldBe 0
    }

    "error(s) if no values are provided" in {
      val fileRow = openMetadataFileRow(rightsCopyRight = None)
      validationErrors(fileRow) should contain theSameElementsAs List(
        ValidationError(
          SCHEMA_REQUIRED,
          "rights_copyright",
          "required"
        )
      )
    }

    "error(s) if 'Unknown' value is provided with another valid value" in {
      val fileRow = openMetadataFileRow(rightsCopyRight = s"Unknown${ARRAY_SPLIT_CHAR}Crown copyright".some)
      validationErrors(fileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_BASE, "rights_copyright", "maxItems")
      )
    }
  }
}
