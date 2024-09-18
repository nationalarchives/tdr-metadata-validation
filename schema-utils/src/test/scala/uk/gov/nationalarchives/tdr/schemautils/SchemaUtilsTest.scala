package uk.gov.nationalarchives.tdr.schemautils

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import SchemaUtils.{convertToAlternateKey, convertToValidationKey, originalKeyToAlternateKeyMapping}

class SchemaUtilsTest extends AnyWordSpec {

  "originalKeyToAlternateKeyMapping" should {
    "return the correct mapping for the given original and alternate key parameters" in {
      val res = originalKeyToAlternateKeyMapping("tdrFileHeader", "tdrDataLoadHeader")
      res.size should be(20)
      res("Filepath") should be("ClientSideOriginalFilepath")
      res("Date last modified") should be("ClientSideFileLastModifiedDate")
      res("Filename") should be("Filename")
      res("Alternative description") should be("DescriptionAlternate")
      res("Is the description sensitive for the public?") should be("DescriptionClosed")
      res("FOI decision asserted") should be("FoiExemptionAsserted")
      res("FOI exemption code") should be("FoiExemptionCode")
      res("Closure status") should be("ClosureType")
      res("Closure Period") should be("ClosurePeriod")
      res("Closure Start Date") should be("ClosureStartDate")
      res("Is the title sensitive for the public?") should be("ClosureType")
      res("Add alternative title without the file extension") should be("TitleAlternate")
    }

    "return base schema property names where original and alternate key parameters do not exist" in {
      val res = originalKeyToAlternateKeyMapping("unknownOriginalKey", "unknownAlternateKey")
      res.size should be(20)
      res.foreach(e => e._1 should equal(e._2))
    }
  }

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

  private val expectedKnownKeyMapping = {
    Map(
      "client_side_checksum" -> "SHA256ClientSideChecksum"
    )
  }

}
