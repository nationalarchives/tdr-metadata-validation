package uk.gov.nationalarchives.tdr.validation.utils

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.nationalarchives.tdr.validation.utils.ConfigUtils.{ConfigParameters, loadBaseSchema, loadConfigFile}

class ConfigUtilsSpec extends AnyWordSpec {

  "load configuration inputToPropertyMapper" in {
    val metadataConfiguration = ConfigUtils.loadConfiguration
    metadataConfiguration.inputToPropertyMapper("tdrFileHeader", "Former reference") shouldBe "former_reference_department"
    metadataConfiguration.inputToPropertyMapper("tdrDataLoadHeader", "former_reference_department") shouldBe "former_reference_department"
    metadataConfiguration.inputToPropertyMapper("tdrFileHeader", "Add alternative title without the file extension") shouldBe "title_alternate"
    metadataConfiguration.inputToPropertyMapper("tdrDataLoadHeader", "TitleAlternate") shouldBe "title_alternate"
  }

  "load configuration propertyToOutputMapper" in {
    val metadataConfiguration = ConfigUtils.loadConfiguration
    metadataConfiguration.propertyToOutputMapper("tdrFileHeader", "former_reference_department") shouldBe "Former reference"
    metadataConfiguration.propertyToOutputMapper("tdrDataLoadHeader", "date_last_modified") shouldBe "ClientSideFileLastModifiedDate"
    metadataConfiguration.propertyToOutputMapper("blah", "blahBlah") shouldBe "blahBlah"
  }

  "load configuration downloadProperties" in {
    val metadataConfiguration = ConfigUtils.loadConfiguration
    metadataConfiguration.downloadProperties("ClientTemplate").length shouldBe 17
    metadataConfiguration.downloadProperties("UnknownClientTemplate").length shouldBe 0
  }

  "load configuration getPropertyType" in {
    val metadataConfiguration = ConfigUtils.loadConfiguration
    metadataConfiguration.getPropertyType("file_path") shouldBe "string"
    metadataConfiguration.getPropertyType("end_date") shouldBe "date"
    metadataConfiguration.getPropertyType("end_dates") shouldBe ""
  }
}
