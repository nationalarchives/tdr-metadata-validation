package uk.gov.nationalarchives.tdr.validation

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaDefinition.BASE_SCHEMA
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaValidators

class JsonSchemaValidatorsSpec extends AnyWordSpec with Matchers {

  "JsonSchemaValidators" when {
    "validating using base schema" should {
      "produce array error for foi_exemption_code" in {
        val json =
          """{
          "foi_exemption_code":"23"
        }"""
        val errors = JsonSchemaValidators.validateJson(BASE_SCHEMA, json)
        errors.iterator.next().getInstanceLocation.getName(0) shouldBe "foi_exemption_code"
      }
      "produce an error for end_date if after today" in {
        val json =
          """{
          "end_date":"2030-12-23"
        }"""
        val errors = JsonSchemaValidators.validateJson(BASE_SCHEMA, json)
        errors.size shouldBe 1
      }
      "produce no error for end_date if before today" in {
        val json =
          """{
          "end_date":"2023-12-23"
        }"""
        val errors = JsonSchemaValidators.validateJson(BASE_SCHEMA, json)
        errors.size shouldBe 0
      }
      "produce only one error for end_date if invalid format" in {
        val json =
          """{
          "end_date":"Wrong"
        }"""
        val errors = JsonSchemaValidators.validateJson(BASE_SCHEMA, json)
        errors.size shouldBe 1

      }
    }
  }
}
