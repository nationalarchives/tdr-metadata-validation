package uk.gov.nationalarchives.tdr.validation.schema.fields

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.nationalarchives.tdr.validation.schema.helpers.TestHelper._

class InvalidColumnTitleSpec extends AnyWordSpecLike {

  "When validating against all schema we see" should {

    "error(s) if an invalid column header is provided" in {
      val openTestFileRow = openMetadataFileRow(invalidColumnTitle = Some("data in a column that with an invalid title"))
      validationErrors(openTestFileRow).size shouldBe 0
      val closedTestFileRow = closedMetadataFileRow(invalidColumnTitle = Some("data in a column that with an invalid title"))
      validationErrors(closedTestFileRow).size shouldBe 0
    }
  }
}
