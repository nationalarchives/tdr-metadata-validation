package uk.gov.nationalarchives.tdr.validation.utils

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class CSVtoJsonUtilsSpec extends AnyWordSpec {

  "CSVtoJsonUtils" should {

    "return mapping between Base Schema property and alternative header" in {
      val utils = new CSVtoJsonUtils()
      val mapping = utils.propertyNameToAlternativeKeyMapping("tdrFileHeader")

      mapping.size shouldBe 18
      mapping should contain theSameElementsAs Map(
        "title_alternate" -> "Add alternative title without the file extension",
        "closure_type" -> "Closure status",
        "description" -> "Description",
        "UUID" -> "UUID",
        "former_reference_department" -> "Former reference",
        "closure_period" -> "Closure Period",
        "language" -> "Language",
        "end_date" -> "Date of the record",
        "file_path" -> "Filepath",
        "foi_exemption_code" -> "FOI exemption code",
        "closure_start_date" -> "Closure Start Date",
        "file_name_translation" -> "Translated title of record",
        "description_closed" -> "Is the description sensitive for the public?",
        "title_closed" -> "Is the title sensitive for the public?",
        "foi_exemption_asserted" -> "FOI decision asserted",
        "date_last_modified" -> "Date last modified",
        "description_alternate" -> "Alternative description",
        "file_name" -> "Filename"
      )
    }

    "return original property name when no alternative key provided" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("Closure Period" -> "5")
      val result = utils.convertToJSONString(testData, None)
      assert(result == """{"Closure Period":"5"}""")
    }

    "return base schema property name when alternative key provided" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("Closure Period" -> "5")
      val result = utils.convertToJSONString(testData, Some("tdrFileHeader"))
      assert(result == """{"closure_period":5}""")
    }

    "correctly convert a numeric string to a JSON `number`" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("Closure Period" -> "5")
      val result = utils.convertToJSONString(testData, Some("tdrFileHeader"))
      assert(result == """{"closure_period":5}""")
    }

    "return a JSON string when the input is not a `number`" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("closure_period" -> "abc")
      val result = utils.convertToJSONString(testData, None)
      assert(result == """{"closure_period":"abc"}""")
    }

    "correctly convert a split an array string to a JSON `array`" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("foi_exemption_code" -> "37(1)(ab)|44")
      val result = utils.convertToJSONString(testData, None)
      assert(result == """{"foi_exemption_code":["37(1)(ab)","44"]}""")
    }

    "return a JSON string when the input array cannot be split" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("foi_exemption_code" -> "37(1)(ab)+44")
      val result = utils.convertToJSONString(testData, None)
      assert(result == """{"foi_exemption_code":["37(1)(ab)+44"]}""")
    }

    "return a empty JSON string when the input array is empty" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("language" -> "")
      val result = utils.convertToJSONString(testData, None)
      assert(result == """{"language":""}""")
    }

    "correctly convert a boolean string to a JSON `boolean`" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("title_closed" -> "Yes", "description_closed" -> "No")
      val result = utils.convertToJSONString(testData, None)
      assert(result == """{"title_closed":true,"description_closed":false}""")
    }

    "return a JSON string when the input boolean cannot be converted" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("title_closed" -> "y", "description_closed" -> "n")
      val result = utils.convertToJSONString(testData, None)
      assert(result == """{"title_closed":"y","description_closed":"n"}""")
    }

    "Preserve key-value pairs when key is not in schema" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("unknown" -> "some value")
      val result = utils.convertToJSONString(testData, None)
      assert(result == """{"unknown":"some value"}""")
    }
  }
}
