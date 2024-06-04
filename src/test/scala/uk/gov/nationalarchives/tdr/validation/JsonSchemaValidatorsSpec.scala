package uk.gov.nationalarchives.tdr.validation

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaDefinition.BASE_SCHEMA
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaValidators

class JsonSchemaValidatorsSpec extends AnyWordSpec with Matchers {

  "JsonSchemaValidators" when {
    "validating using base schema" should {
      "produce a unionType error for foi_exemption_code that is not in the FOI code definitions" in {
        val json =
          """{
          "foi_exemption_code":"23"
        }"""
        val errors = JsonSchemaValidators.validateJson(BASE_SCHEMA, json)
        val error = errors.iterator.next()
        error.getInstanceLocation.getName(0) shouldBe "foi_exemption_code"
        error.getMessageKey shouldBe "unionType"
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
      "produce two errors for end_date if invalid format - one in standard schema check and one from daBeforeData" in {
        val json =
          """{
          "end_date":"Wrong"
        }"""
        val errors = JsonSchemaValidators.validateJson(BASE_SCHEMA, json)
        errors.size shouldBe 2
      }
    }
  }
}
