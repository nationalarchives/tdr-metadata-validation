package uk.gov.nationalarchives.tdr.validation

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaDefinition.BASE_SCHEMA
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaMetadataValidation

class JsonSchemaMetadataValidationSpec extends AnyWordSpec with Matchers {

  "JsonSchemaMetadataValidation" when {
    "validating using base schema" should {
      "do something useful when complete" in {
        val fileRow = FileRow("myFilePath", List[Metadata]())
        val errors: Map[String, List[Error]] = JsonSchemaMetadataValidation.validateMetadata(BASE_SCHEMA, List[FileRow](fileRow))
        errors.size shouldBe 1
        println(errors)
        errors.get("myFilePath").get.toArray.iterator.next().errorCode shouldBe "daDateBefore"
      }
    }
  }
}
