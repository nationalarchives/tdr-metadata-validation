package uk.gov.nationalarchives.tdr.schemautils

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import SchemaUtils.{baseSchemaNode, convertToAlternateKey, convertToValidationKey, getClass, schemaProperties}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}

class SchemaUtilsTest extends AnyWordSpec {

  "schemaProperties" should {
    "return the properties for the base schema where no schema node provided" in {
      val properties = schemaProperties()
      properties.size should be(20)
    }

    "return the properties of the schema node provided" in {
      val otherSchema = "/metadata-schema/dataLoadSharePointSchema.schema.json"
      val otherSchemaNode: JsonNode = new ObjectMapper().readTree(getClass.getResourceAsStream(otherSchema))

      val properties = schemaProperties(otherSchemaNode)
      properties.size should be(7)
    }
  }

  "baseSchemaNode" should {

    "return the base schema property from the supplied '$ref' schema reference if property exists in base schema" in {
      val result = baseSchemaNode("classpath:/metadata-schema/baseSchema.schema.json#/properties/file_path")
      result.isDefined should be(true)
      result.get._1 should be("file_path")
    }

    "return 'None' if the supplied '$ref' schema reference does not exist in the base schema" in {
      val result = baseSchemaNode("classpath:/metadata-schema/baseSchema.schema.json#/properties/unknown_property")
      result.isDefined should be(false)
    }

    "return 'None' if an invalid '$ref' schema reference is supplied" in {
      val result = baseSchemaNode("invalid-ref/")
      result.isDefined should be(false)
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

    "convert property key to an alternate key for the base schema where no schema node provided" in {
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
