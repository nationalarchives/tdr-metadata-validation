package uk.gov.nationalarchives.tdr.validation.utils

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.nationalarchives.tdr.validation.utils.ConfigUtils.{ConfigParameters, loadBaseSchema, loadConfigFile}

class ConfigUtilsSpec extends AnyWordSpec {

  "ConfigUtils" should {

    "correctly convert a domain key to a property`" in {
      val configParameters: ConfigParameters = ConfigParameters(loadBaseSchema, loadConfigFile)
      val inputToPropertyMapper: (String, String) => String = ConfigUtils.inputToPropertyMapper(configParameters)
      inputToPropertyMapper("number", "123") shouldBe "123"
      inputToPropertyMapper("tdrFileHeader", "Former reference") shouldBe "former_reference_department"
    }
  }
  "correctly convert a property to a domain key`" in {
    val configParameters: ConfigParameters = ConfigParameters(loadBaseSchema, loadConfigFile)
    val propertyToOutputMapper: (String, String) => String = ConfigUtils.propertyToOutputMapper(configParameters)
    propertyToOutputMapper("number", "123") shouldBe "123"
    propertyToOutputMapper("tdrFileHeader", "former_reference_department") shouldBe "Former reference"
  }

  "load configuration" in {
    val configParameters: ConfigParameters = ConfigParameters(loadBaseSchema, loadConfigFile)
    val metadataConfiguration = ConfigUtils.loadConfiguration(configParameters)
    metadataConfiguration.inputToPropertyMapper("tdrFileHeader", "Former reference") shouldBe "former_reference_department"
    metadataConfiguration.propertyToOutputMapper("tdrFileHeader", "former_reference_department") shouldBe "Former reference"
    metadataConfiguration.downloadProperties("Base") shouldBe List(("file_path", 1), ("file_name", 2), ("date_last_modified", 3), ("closure_type", 10))
  }
}
