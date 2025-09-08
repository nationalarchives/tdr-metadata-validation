package uk.gov.nationalarchives.tdr.validation.schema.fields

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.nationalarchives.tdr.validation.schema.ValidationError
import uk.gov.nationalarchives.tdr.validation.schema.ValidationProcess.{SCHEMA_BASE, SCHEMA_CLOSURE_CLOSED, SCHEMA_CLOSURE_OPEN, SCHEMA_REQUIRED}
import uk.gov.nationalarchives.tdr.validation.schema.helpers.TestHelper._
import uk.gov.nationalarchives.tdr.schemautils.ConfigUtils.ARRAY_SPLIT_CHAR

class FoiExemptionCodeSpec extends AnyWordSpecLike {

  "When validating against all schema we see " should {

    "success if the value is missing for an open record" in {
      val openTestFileRow = openMetadataFileRow(foiCodes = Some(""))
      validationErrors(openTestFileRow).size shouldBe 0
    }

    "success if a single value is valid" in {
      val closedTestFileRow = closedMetadataFileRow(foiCodes = Some("37(1)(a)"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "success if another single value is valid" in {
      val closedTestFileRow = closedMetadataFileRow(foiCodes = Some("38"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "success if two valid values are provided" in {
      val closedTestFileRow = closedMetadataFileRow(foiCodes = Some(s"38${ARRAY_SPLIT_CHAR}EIR 12(5)(c)"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "success if all possible valid values are provided" in {
      val closedTestFileRow = closedMetadataFileRow(foiCodes =
        Some(
          s"23${ARRAY_SPLIT_CHAR}24${ARRAY_SPLIT_CHAR}26${ARRAY_SPLIT_CHAR}27(1)${ARRAY_SPLIT_CHAR}27(2)${ARRAY_SPLIT_CHAR}28${ARRAY_SPLIT_CHAR}29${ARRAY_SPLIT_CHAR}30(1)${ARRAY_SPLIT_CHAR}30(2)${ARRAY_SPLIT_CHAR}31${ARRAY_SPLIT_CHAR}32${ARRAY_SPLIT_CHAR}33${ARRAY_SPLIT_CHAR}34${ARRAY_SPLIT_CHAR}35(1)(a)" +
            s"${ARRAY_SPLIT_CHAR}35(1)(b)${ARRAY_SPLIT_CHAR}35(1)(c)${ARRAY_SPLIT_CHAR}35(1)(d)${ARRAY_SPLIT_CHAR}36${ARRAY_SPLIT_CHAR}37(1)(a)${ARRAY_SPLIT_CHAR}37(1)(aa)${ARRAY_SPLIT_CHAR}37(1)(ab)${ARRAY_SPLIT_CHAR}37(1)(ac)" +
            s"${ARRAY_SPLIT_CHAR}37(1)(ad)${ARRAY_SPLIT_CHAR}37(1)(b)${ARRAY_SPLIT_CHAR}38${ARRAY_SPLIT_CHAR}39${ARRAY_SPLIT_CHAR}40(2)${ARRAY_SPLIT_CHAR}41${ARRAY_SPLIT_CHAR}42${ARRAY_SPLIT_CHAR}43(1)${ARRAY_SPLIT_CHAR}43(2)${ARRAY_SPLIT_CHAR}44${ARRAY_SPLIT_CHAR}EIR 12(5)(a)${ARRAY_SPLIT_CHAR}" +
            s"EIR 12(5)(b)${ARRAY_SPLIT_CHAR}EIR 12(5)(c)${ARRAY_SPLIT_CHAR}EIR 12(5)(d)${ARRAY_SPLIT_CHAR}EIR 12(5)(e)${ARRAY_SPLIT_CHAR}EIR 12(5)(f)${ARRAY_SPLIT_CHAR}EIR 12(5)(g)"
        )
      )
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "succeeds if a single value is followed by a separator" in {
      val closedTestFileRow = closedMetadataFileRow(foiCodes = Some(s"39$ARRAY_SPLIT_CHAR"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "error(s) if the column is missing" in {
      val openTestFileRow = openMetadataFileRow(foiCodes = None)
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_REQUIRED, "foi_exemption_code", "required")
      )
      val closedTestFileRow = closedMetadataFileRow(foiCodes = None)
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_REQUIRED, "foi_exemption_code", "required")
      )
    }

    "error(s) if there a value is provided for an open record" in {
      val openTestFileRow = openMetadataFileRow(foiCodes = Some("44"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_OPEN, "foi_exemption_code", "type") // Must be empty for an open record
      )
    }

    "error(s) if there is no value in the cell for a closed record" in {
      val closedTestFileRow = closedMetadataFileRow(foiCodes = Some(""))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(SCHEMA_CLOSURE_CLOSED, "foi_exemption_code", "type") // Must be provided for a closed record
      )
    }

    "error(s) if the value is invalid for a closed record" in {
      val closedTestFileRow = closedMetadataFileRow(foiCodes = Some("999"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(
          SCHEMA_BASE,
          "foi_exemption_code",
          "enum"
        ) // Must be a pipe delimited list of valid FOI codes, (eg. 31|33). Please see the guidance for more detail on valid codes
      )
    }

    "error(s) if there is white space around the value for a closed record" in {
      val closedTestFileRow = closedMetadataFileRow(foiCodes = Some("44 "))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(
          SCHEMA_BASE,
          "foi_exemption_code",
          "enum"
        ) // Must be a pipe delimited list of valid FOI codes, (eg. 31|33). Please see the guidance for more detail on valid codes
      )
    }

    "error(s) if there is white space around a list of values for a closed record" in {
      val closedTestFileRow = closedMetadataFileRow(foiCodes = Some("44 | 41"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(
          SCHEMA_BASE,
          "foi_exemption_code",
          "enum"
        ) // Must be a pipe delimited list of valid FOI codes, (eg. 31|33). Please see the guidance for more detail on valid codes
      )
    }

    "error(s) if the value is not valid for a closed record" in {
      val closedTestFileRow = closedMetadataFileRow(foiCodes = Some("BLAH or other invalid"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(
          SCHEMA_BASE,
          "foi_exemption_code",
          "enum"
        ) // Must be a pipe delimited list of valid FOI codes, (eg. 31|33). Please see the guidance for more detail on valid codes
      )
    }

    "error(s) if one value is valid and the other invalid for a closed record" in {
      val closedTestFileRow = closedMetadataFileRow(foiCodes = Some("44|999"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(
          SCHEMA_BASE,
          "foi_exemption_code",
          "enum"
        ) // Must be a pipe delimited list of valid FOI codes, (eg. 31|33). Please see the guidance for more detail on valid codes
      )
    }

    "error(s) if a separator is at the front of a list" in {
      val closedTestFileRow = closedMetadataFileRow(foiCodes = Some(";44;38"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(
          SCHEMA_BASE,
          "foi_exemption_code",
          "enum"
        ) // Must be a pipe delimited list of valid FOI codes, (eg. 31|33). Please see the guidance for more detail on valid codes
      )
    }

    "error(s) if the value is a boolean" in {
      val closedTestFileRow = closedMetadataFileRow(foiCodes = Some("Yes"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(
          SCHEMA_BASE,
          "foi_exemption_code",
          "enum"
        ) // Must be a pipe delimited list of valid FOI codes, (eg. 31|33). Please see the guidance for more detail on valid codes
      )
    }
  }
}
