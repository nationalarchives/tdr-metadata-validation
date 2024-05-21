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
        errors.toArray.iterator.next().propertyName shouldBe "foi_exemption_code"
      }
      "produce an error for date_last_modified if after today" in {
        val json =
          """{
          "date_last_modified":"2030-12-23"
        }"""
        val errors = JsonSchemaValidators.validateJson(BASE_SCHEMA, json)
        errors.contains(Error("date_last_modified", "daDateBefore")) shouldBe true
      }
      "produce no error for date_last_modified if before today" in {
        val json =
          """{
          "date_last_modified":"2023-12-23"
        }"""
        val errors = JsonSchemaValidators.validateJson(BASE_SCHEMA, json)
        errors.size shouldBe 0
      }
      "produce only one error for date_last_modified if invalid format" in {
        val json =
          """{
          "date_last_modified":"Wrong"
        }"""
        val errors = JsonSchemaValidators.validateJson(BASE_SCHEMA, json)
        errors.size shouldBe 1
        errors.contains(Error("date_last_modified", "format.date")) shouldBe true
      }
    }
  }
}
