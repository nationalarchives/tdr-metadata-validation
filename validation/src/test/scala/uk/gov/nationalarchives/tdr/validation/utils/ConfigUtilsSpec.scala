package uk.gov.nationalarchives.tdr.validation.utils

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class ConfigUtilsSpec extends AnyWordSpec {

  "ConfigUtils should load configuration and provide an inputToPropertyMapper method that" should {
    "give the base Schema property for a domain key" in {
      val metadataConfiguration = ConfigUtils.loadConfiguration
      val tdrFileHeaderMapper = metadataConfiguration.inputToPropertyMapper("tdrFileHeader")
      tdrFileHeaderMapper("former reference") shouldBe "former_reference_department"
      tdrFileHeaderMapper("alternate filename") shouldBe "title_alternate"
      metadataConfiguration.inputToPropertyMapper("tdrDataLoadHeader")("former_reference_department") shouldBe "former_reference_department"
      metadataConfiguration.inputToPropertyMapper("tdrDataLoadHeader")("DescriptionClosed") shouldBe "description_closed"
    }
  }

  "ConfigUtils should load configuration and provide a propertyToOutputMapper method that" should {
    "give the domain key for a given property" in {
      val metadataConfiguration = ConfigUtils.loadConfiguration
      metadataConfiguration.propertyToOutputMapper("tdrFileHeader")("former_reference_department") shouldBe "former reference"
      metadataConfiguration.propertyToOutputMapper("tdrDataLoadHeader")("date_last_modified") shouldBe "ClientSideFileLastModifiedDate"
      metadataConfiguration.propertyToOutputMapper("blah")("blahBlah") shouldBe "blahBlah"
    }
  }

  "ConfigUtils should load configuration and provide a getPropertyType method that" should {
    "give the type config of a specified property" in {
      val metadataConfiguration = ConfigUtils.loadConfiguration
      metadataConfiguration.getPropertyType("file_path") shouldBe "string"
      metadataConfiguration.getPropertyType("end_date") shouldBe "date"
      metadataConfiguration.getPropertyType("end_dates") shouldBe ""
    }
  }

  "ConfigUtils should load configuration and provide a downloadProperties method that" should {
    "give the downloadProperties config for a specified download" in {
      val metadataConfiguration = ConfigUtils.loadConfiguration
      metadataConfiguration.downloadFileDisplayProperties("MetadataDownloadTemplate").length shouldBe 17
      metadataConfiguration.downloadFileDisplayProperties("UnknownClientTemplate").length shouldBe 0
    }

    "return the correct DownloadFilesOutput objects for a valid domain" in {
      val metadataConfiguration = ConfigUtils.loadConfiguration
      val outputs = metadataConfiguration.downloadFileDisplayProperties("MetadataDownloadTemplate")
      outputs.head.key shouldBe "file_path"
      outputs.head.columnIndex shouldBe 1
      outputs.head.editable shouldBe false
    }

    "return an empty list for an invalid domain" in {
      val metadataConfiguration = ConfigUtils.loadConfiguration
      metadataConfiguration.downloadFileDisplayProperties("InvalidDomain") shouldBe empty
    }
  }

}
