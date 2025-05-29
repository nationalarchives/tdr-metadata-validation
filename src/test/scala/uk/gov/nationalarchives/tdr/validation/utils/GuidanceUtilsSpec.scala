package uk.gov.nationalarchives.tdr.validation.utils

import io.circe.Json
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GuidanceUtilsSpec extends AnyFlatSpec with Matchers {

  import GuidanceUtils._
  "decodeExample" should "decode string values correctly" in {
    val json = Json.fromString("test string")
    val cursor = json.hcursor

    decodeExample.apply(cursor) shouldBe Right("test string")
  }

  it should "decode number values as strings" in {
    val json = Json.fromInt(123)
    val cursor = json.hcursor

    decodeExample.apply(cursor) shouldBe Right("123")
  }

  "decodeGuidanceItem" should "decode a complete GuidanceItem correctly" in {
    val json = Json.obj(
      "property" -> Json.fromString("file_path"),
      "details" -> Json.fromString("This is the file path/folder structure extracted upon upload, do not modify"),
      "format" -> Json.fromString("Do not modify"),
      "tdrRequirement" -> Json.fromString("Mandatory"),
      "example" -> Json.fromString("N/A")
    )

    decodeGuidanceItem.decodeJson(json) shouldBe Right(
      GuidanceItem(
        property = "file_path",
        details = "This is the file path/folder structure extracted upon upload, do not modify",
        format = "Do not modify",
        tdrRequirement = "Mandatory",
        example = "N/A"
      )
    )
  }

  it should "decode GuidanceItem with numeric example" in {
    val json = Json.obj(
      "property" -> Json.fromString("closure_period"),
      "details" -> Json.fromString("Closed record: Provide the number of years that the record will remain closed"),
      "format" -> Json.fromString("Number (1-150)"),
      "tdrRequirement" -> Json.fromString("Mandatory for closed record"),
      "example" -> Json.fromInt(20)
    )
    decodeGuidanceItem.decodeJson(json) shouldBe Right(
      GuidanceItem(
        property = "closure_period",
        details = "Closed record: Provide the number of years that the record will remain closed",
        format = "Number (1-150)",
        tdrRequirement = "Mandatory for closed record",
        example = "20"
      )
    )
  }
}
