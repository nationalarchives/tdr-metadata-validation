package uk.gov.nationalarchives.tdr.validation

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaDefinitions.BASE_SCHEMA
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaValidators

class JsonSchemaValidatorsSpec extends AnyWordSpec with Matchers {

  "JsonSchemaValidators" when {
    "validating using base schema" should {
      "produce array error for foi_exemption_code" in {
        val json = """{
          "foi_exemption_code":"23"
        }"""
        val errors = JsonSchemaValidators.validateJson(BASE_SCHEMA, json)
        errors.iterator.next().getInstanceLocation.getName(0) shouldBe "foi_exemption_code"
      }
      "produce an error for date_last_modified if after today" in {
        val json = """{
          "date_last_modified":"2035-12-23",
          "XXXX":"23"
        }"""
        val errors = JsonSchemaValidators.validateJson(BASE_SCHEMA, json)
        errors.iterator.next().getInstanceLocation.getName(0) shouldBe "date_last_modified"
      }
    }
  }
}
