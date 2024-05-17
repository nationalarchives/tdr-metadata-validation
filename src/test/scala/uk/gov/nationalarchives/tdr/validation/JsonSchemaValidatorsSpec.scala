package uk.gov.nationalarchives.tdr.validation

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaDefinitions.BASE_SCHEMA
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaValidators
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaValidators.ValidationError

import java.util

class JsonSchemaValidatorsSpec extends AnyWordSpec with Matchers {

  "JsonSchemaValidators" when {
    "validating using base schema" should {
      "produce array error for foi_exemption_code" in {
        val json =
          """{
          "foi_exemption_code":"23"
        }"""
        val errors: util.Set[ValidationError] = JsonSchemaValidators.validateJson(BASE_SCHEMA, json)
        errors.iterator().next().property shouldBe "foi_exemption_code"
      }
      "produce an error for date_last_modified if after today" in {
        val json =
          """{
          "date_last_modified":"2030-12-23"
        }"""
        val errors = JsonSchemaValidators.validateJson(BASE_SCHEMA, json)
        errors.iterator().next().property shouldBe "date_last_modified"
      }
    }
  }
}
