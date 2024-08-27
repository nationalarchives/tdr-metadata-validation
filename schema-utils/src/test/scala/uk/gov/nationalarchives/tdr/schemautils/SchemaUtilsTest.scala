package uk.gov.nationalarchives.tdr.schemautils

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import SchemaUtils.{convertToAlternateKey, convertToValidationKey}

class SchemaUtilsTest extends AnyWordSpec {

  "convertToValidationKey" should {

    "convert alternate key to a property key" in {
      convertToValidationKey("tdrFileHeader", "Date last modified") should be("date_last_modified")
      convertToValidationKey("tdrDataLoadHeader", "ClientSideOriginalFilepath") should be("file_path")
    }

    "return empty string if the alternate key name is not valid" in {
      convertToValidationKey("dddd", "Date last modified") should be("")
    }

    "return empty string if the alternate value name is not valid" in {
      convertToValidationKey("tdrFileHeader", "ddddd") should be("")
    }
  }

  "convertToAlternateKey" should {

    "convert property key to an alternate key" in {
      convertToAlternateKey("tdrFileHeader", "date_last_modified") should be("Date last modified")
      convertToAlternateKey("tdrDataLoadHeader", "file_path") should be("ClientSideOriginalFilepath")
    }

    "return empty string if the property key is not valid" in {
      convertToValidationKey("tdrFileHeader", "dddd") should be("")
    }

    "return empty string if the property key is valid but the alternate key name is not valid" in {
      convertToValidationKey("dddd", "date_last_modified") should be("")
    }
  }

}