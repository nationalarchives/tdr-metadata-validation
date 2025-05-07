package uk.gov.nationalarchives.tdr.schemautils

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import SchemaUtils._

class SchemaUtilsTest extends AnyWordSpec {

  "convertToValidationKey" should {

    "convert alternate key to a property key" in {
      convertToValidationKey("tdrFileHeader", "date last modified") should be("date_last_modified")
      convertToValidationKey("tdrDataLoadHeader", "ClientSideOriginalFilepath") should be("file_path")
    }

    "return empty string if the alternate key name is not valid" in {
      convertToValidationKey("dddd", "date last modified") should be("")
    }

    "return empty string if the alternate value name is not valid" in {
      convertToValidationKey("tdrFileHeader", "ddddd") should be("")
    }
  }

  "getMetadataProperties" should {
    "return list of properties by given propertyType" in {
      getMetadataProperties("System").size should be(6)
    }
  }

  "convertToAlternateKey" should {

    "convert property key to an alternate key" in {
      convertToAlternateKey("tdrFileHeader", "date_last_modified") should be("date last modified")
      convertToAlternateKey("tdrDataLoadHeader", "file_path") should be("ClientSideOriginalFilepath")
    }

    "return empty string if the property key is not valid" in {
      convertToValidationKey("tdrFileHeader", "dddd") should be("")
    }

    "return empty string if the property key is valid but the alternate key name is not valid" in {
      convertToValidationKey("dddd", "date_last_modified") should be("")
    }
  }

  "getPropertyField" should {

    "return empty string if the property field is not present" in {
      getPropertyField("file_path", "null_field").asText("") should be("")
    }

    "return correct value if property field is present" in {
      getPropertyField("file_path", "propertyType").asText() should be("System")
    }
  }

}
