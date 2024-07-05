package uk.gov.nationalarchives.tdr.validation.utils

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class CSVtoJsonUtilsSpec extends AnyWordSpec {

  "CSVtoJsonUtils" should {

    "correctly convert a numeric string to a JSON `number`" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("Closure Period" -> "5")
      val result = utils.convertToJSONString(testData, "tdrFileHeader")
      assert(result == """{"closure_period":5}""")
    }

    "return a JSON string when the input is not a `number`" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("Closure Period" -> "abc")
      val result = utils.convertToJSONString(testData, "tdrFileHeader")
      assert(result == """{"closure_period":"abc"}""")
    }

    "correctly convert a split an array string to a JSON `array`" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("FOI exemption code" -> "37(1)(ab)|44")
      val result = utils.convertToJSONString(testData, "tdrFileHeader")
      assert(result == """{"foi_exemption_code":["37(1)(ab)","44"]}""")
    }

    "return a JSON string when the input array cannot be split" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("FOI exemption code" -> "37(1)(ab)+44")
      val result = utils.convertToJSONString(testData, "tdrFileHeader")
      assert(result == """{"foi_exemption_code":["37(1)(ab)+44"]}""")
    }

    "return a empty JSON string when the input array is empty" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("Language" -> "")
      val result = utils.convertToJSONString(testData, "tdrFileHeader")
      assert(result == """{"language":""}""")
    }

    "correctly convert a boolean string to a JSON `boolean`" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("Is the title sensitive for the public?" -> "Yes", "Is the description sensitive for the public?" -> "No")
      val result = utils.convertToJSONString(testData, "tdrFileHeader")
      assert(result == """{"title_closed":true,"description_closed":false}""")
    }

    "return a JSON string when the input boolean cannot be converted" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("Is the title sensitive for the public?" -> "y", "Is the description sensitive for the public?" -> "n")
      val result = utils.convertToJSONString(testData, "tdrFileHeader")
      assert(result == """{"title_closed":"y","description_closed":"n"}""")
    }

    "Preserve key-value pairs when key is not in schema" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("unknown" -> "some value")
      val result = utils.convertToJSONString(testData, "tdrFileHeader")
      assert(result == """{"unknown":"some value"}""")
    }
  }
}
