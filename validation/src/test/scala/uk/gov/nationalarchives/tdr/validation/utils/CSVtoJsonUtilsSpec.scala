package uk.gov.nationalarchives.tdr.validation.utils

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.nationalarchives.tdr.validation.utils.ConfigUtils.ARRAY_SPLIT_CHAR

class CSVtoJsonUtilsSpec extends AnyWordSpec {

  "CSVtoJsonUtils" should {

    "correctly convert a numeric string to a JSON `number`" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("closure period" -> "5")
      val result = utils.convertToJSONString(testData)
      assert(result == """{"closure_period":[5]}""")
    }

    "return a JSON string when the input is not a `number`" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("closure period" -> "abc")
      val result = utils.convertToJSONString(testData)
      assert(result == """{"closure_period":["abc"]}""")
    }

    "correctly convert a split an array string to a JSON `array`" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("foi exemption code" -> s"37(1)(ab)${ARRAY_SPLIT_CHAR}44")
      val result = utils.convertToJSONString(testData)
      assert(result == """{"foi_exemption_code":["37(1)(ab)","44"]}""")
    }

    "return a JSON string when the input array cannot be split" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("foi exemption code" -> "37(1)(ab)+44")
      val result = utils.convertToJSONString(testData)
      assert(result == """{"foi_exemption_code":["37(1)(ab)+44"]}""")
    }

    "return a empty JSON string when the input array is empty" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("language" -> "")
      val result = utils.convertToJSONString(testData)
      assert(result == """{"language":""}""")
    }

    "correctly convert a boolean string to a JSON `boolean`" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("is filename closed" -> "Yes", "is description closed" -> "No")
      val result = utils.convertToJSONString(testData)
      assert(result == """{"title_closed":true,"description_closed":false}""")
    }

    "return a JSON string when the input boolean cannot be converted" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("is filename closed" -> "y", "is description closed" -> "n")
      val result = utils.convertToJSONString(testData)
      assert(result == """{"title_closed":"y","description_closed":"n"}""")
    }

    "Preserve key-value pairs when key is not in schema" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("unknown" -> "some value")
      val result = utils.convertToJSONString(testData)
      assert(result == """{"unknown":"some value"}""")
    }
  }
}
