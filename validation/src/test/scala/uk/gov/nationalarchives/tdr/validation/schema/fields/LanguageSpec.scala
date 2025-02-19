package uk.gov.nationalarchives.tdr.validation.schema.fields

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.nationalarchives.tdr.validation.schema.ValidationError
import uk.gov.nationalarchives.tdr.validation.schema.ValidationProcess.SCHEMA_BASE
import uk.gov.nationalarchives.tdr.validation.schema.helpers.TestHelper._

class LanguageSpec extends AnyWordSpecLike {

  "When validating against all schema we see " should {

    "success if the value is English" in {
      val openTestFileRow = openMetadataFileRow(language = Some("English"))
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(language = Some("English"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "success if the value is Welsh" in {
      val openTestFileRow = openMetadataFileRow(language = Some("Welsh"))
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(language = Some("Welsh"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "success if the value is English AND Welsh" in {
      val openTestFileRow = openMetadataFileRow(language = Some("English|Welsh"))
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(language = Some("English|Welsh"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "succeeds if a single value is followed by a pipe" in {
      val openTestFileRow = openMetadataFileRow(language = Some("English|"))
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(language = Some("Welsh|"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "succeeds if the column is missing" in {
      val openTestFileRow = openMetadataFileRow(language = None)
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(language = None)
      validationErrors(closedTestFileRow).size shouldBe 0
    }

    "error(s) if there is no value in the cell" in {
      val openTestFileRow = openMetadataFileRow(language = Some(""))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(
          SCHEMA_BASE,
          "language",
          "type"
        ) // Language must be capitalised English or Welsh, if a record is in both languages you must use a pipe separated list, ie. English|Welsh
      )
      val closedTestFileRow = closedMetadataFileRow(language = Some(""))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(
          SCHEMA_BASE,
          "language",
          "type"
        ) // Language must be capitalised English or Welsh, if a record is in both languages you must use a pipe separated list, ie. English|Welsh
      )
    }

    "error(s) if the value is english (no capital E for English)" in {
      val openTestFileRow = openMetadataFileRow(language = Some("english"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(
          SCHEMA_BASE,
          "language",
          "enum"
        ) // Language must be one of a valid list, please refer to guidance for details of allowed values. If you cannot see the language you need in this list, please contact your Digital Transfer Advisor.
      )
      val closedTestFileRow = closedMetadataFileRow(language = Some("english"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(
          SCHEMA_BASE,
          "language",
          "enum"
        ) // Language must be one of a valid list, please refer to guidance for details of allowed values. If you cannot see the language you need in this list, please contact your Digital Transfer Advisor.
      )
    }

    "error(s) if there is white space around the value" in {
      val openTestFileRow = openMetadataFileRow(language = Some(" English"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(
          SCHEMA_BASE,
          "language",
          "enum"
        ) // Language must be one of a valid list, please refer to guidance for details of allowed values. If you cannot see the language you need in this list, please contact your Digital Transfer Advisor.
      )
      val closedTestFileRow = closedMetadataFileRow(language = Some("English "))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(
          SCHEMA_BASE,
          "language",
          "enum"
        ) // Language must be one of a valid list, please refer to guidance for details of allowed values. If you cannot see the language you need in this list, please contact your Digital Transfer Advisor.
      )
    }

    "error(s) if there is white space around a list of values" in {
      val openTestFileRow = openMetadataFileRow(language = Some("English | Welsh"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(
          SCHEMA_BASE,
          "language",
          "enum"
        ) // Language must be one of a valid list, please refer to guidance for details of allowed values. If you cannot see the language you need in this list, please contact your Digital Transfer Advisor.
      )
      val closedTestFileRow = closedMetadataFileRow(language = Some("English | Welsh"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(
          SCHEMA_BASE,
          "language",
          "enum"
        ) // Language must be one of a valid list, please refer to guidance for details of allowed values. If you cannot see the language you need in this list, please contact your Digital Transfer Advisor.
      )
    }

    "error(s) if the value is not valid" in {
      val openTestFileRow = openMetadataFileRow(language = Some("Spanish or other invalid"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(
          SCHEMA_BASE,
          "language",
          "enum"
        ) // Language must be one of a valid list, please refer to guidance for details of allowed values. If you cannot see the language you need in this list, please contact your Digital Transfer Advisor.
      )
      val closedTestFileRow = closedMetadataFileRow(language = Some("Spanish or other invalid"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(
          SCHEMA_BASE,
          "language",
          "enum"
        ) // Language must be one of a valid list, please refer to guidance for details of allowed values. If you cannot see the language you need in this list, please contact your Digital Transfer Advisor.
      )
    }

    "error(s) if the value is English AND Spanish" in {
      val openTestFileRow = openMetadataFileRow(language = Some("English|Spanish"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(
          SCHEMA_BASE,
          "language",
          "enum"
        ) // Language must be one of a valid list, please refer to guidance for details of allowed values. If you cannot see the language you need in this list, please contact your Digital Transfer Advisor.
      )
      val closedTestFileRow = closedMetadataFileRow(language = Some("English|Spanish"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(
          SCHEMA_BASE,
          "language",
          "enum"
        ) // Language must be one of a valid list, please refer to guidance for details of allowed values. If you cannot see the language you need in this list, please contact your Digital Transfer Advisor.
      )
    }

    "error(s) if a pipe is at the front of a list" in {
      val openTestFileRow = openMetadataFileRow(language = Some("|English"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(
          SCHEMA_BASE,
          "language",
          "enum"
        ), // Language must be one of a valid list, please refer to guidance for details of allowed values. If you cannot see the language you need in this list, please contact your Digital Transfer Advisor.
        ValidationError(
          SCHEMA_BASE,
          "language",
          "type"
        ) // Language must be capitalised English or Welsh, if a record is in both languages you must use a pipe separated list, ie. English|Welsh
      )
      val closedTestFileRow = closedMetadataFileRow(language = Some("|English|Welsh"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(
          SCHEMA_BASE,
          "language",
          "enum"
        ), // Language must be one of a valid list, please refer to guidance for details of allowed values. If you cannot see the language you need in this list, please contact your Digital Transfer Advisor.
        ValidationError(
          SCHEMA_BASE,
          "language",
          "type"
        ) // Language must be capitalised English or Welsh, if a record is in both languages you must use a pipe separated list, ie. English|Welsh
      )
    }

    "error(s) if the value is a boolean" in {
      val openTestFileRow = openMetadataFileRow(language = Some("Yes"))
      validationErrors(openTestFileRow) should contain theSameElementsAs List(
        ValidationError(
          SCHEMA_BASE,
          "language",
          "enum"
        ) // Language must be one of a valid list, please refer to guidance for details of allowed values. If you cannot see the language you need in this list, please contact your Digital Transfer Advisor.
      )
      val closedTestFileRow = closedMetadataFileRow(language = Some("Yes"))
      validationErrors(closedTestFileRow) should contain theSameElementsAs List(
        ValidationError(
          SCHEMA_BASE,
          "language",
          "enum"
        ) // Language must be one of a valid list, please refer to guidance for details of allowed values. If you cannot see the language you need in this list, please contact your Digital Transfer Advisor.
      )
    }
  }
}
