package uk.gov.nationalarchives.tdr.validation

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaDefinition.{BASE_SCHEMA, RELATIONSHIP_SCHEMA}
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaValidators

class JsonSchemaValidatorsSpec extends AnyWordSpec with Matchers {
  "JsonSchemaValidators" when {
    "validating using base schema" should {
      "produce a type error for foi_exemption_code that is not in the FOI code definitions" in {
        val json =
          """{
          "foi_exemption_code":"23"
        }"""
        val errors = JsonSchemaValidators.validateJson(BASE_SCHEMA, json)
        val error = errors.iterator.next()
        error.getInstanceLocation.getName(0) shouldBe "foi_exemption_code"
        error.getMessageKey shouldBe "type"
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
      "produce an error for end_date if invalid format - only from standard schema check" in {
        val json =
          """{
          "end_date":"Wrong"
        }"""
        val errors = JsonSchemaValidators.validateJson(BASE_SCHEMA, json)
        errors.size shouldBe 1
      }
      "produce a pattern error for former_reference_department, file_name_translation and title_alternate if it contains linebreaks" in {
        val json =
          """{
          "former_reference_department":"hello\nworld",
          "file_name_translation":"hello\nworld",
          "title_alternate":"hello\nworld"
        }"""
        val errors = JsonSchemaValidators.validateJson(BASE_SCHEMA, json)
        errors.size shouldBe 3
        errors.foreach { error =>
          error.getInstanceLocation.getName(0) should (
            be("former_reference_department") or
              be("file_name_translation") or
              be("title_alternate")
          )
          error.getMessageKey shouldBe "pattern"
        }
      }
      "validating using relationship schema" should {
        "produce a type error for empty description when alternate description set" in {
          val json =
            """{
          "description_alternate":"alternate",
          "description": null
          }"""
          val errors = JsonSchemaValidators.validateJson(RELATIONSHIP_SCHEMA, json)
          val error = errors.iterator.next()
          error.getInstanceLocation.getName(0) shouldBe "description"
          error.getMessageKey shouldBe "type"
        }

        "produce a type error for copyright_details when rights_copyright Unknown" in {
          val json =
            """{
          "rights_copyright":"Unknown",
          "copyright_details": "should be null"
          }"""
          val errors = JsonSchemaValidators.validateJson(RELATIONSHIP_SCHEMA, json)
          val error = errors.iterator.next()
          error.getInstanceLocation.getName(0) shouldBe "copyright_details"
          error.getMessageKey shouldBe "type"
        }

        "produce no error for null copyright_details when rights_copyright Unknown" in {
          val json =
            """{
          "rights_copyright":"Unknown",
          "copyright_details": null
          }"""
          val errors = JsonSchemaValidators.validateJson(RELATIONSHIP_SCHEMA, json)
          errors.size shouldBe 0
        }
      }
    }
  }
}
