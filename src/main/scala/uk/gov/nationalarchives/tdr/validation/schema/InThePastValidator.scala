package uk.gov.nationalarchives.tdr.validation.schema

import com.fasterxml.jackson.databind.JsonNode
import com.networknt.schema.{AbstractJsonValidator, AbstractKeyword, ExecutionContext, JsonNodePath, JsonSchema, JsonValidator, SchemaLocation, ValidationContext, ValidationMessage}

import java.util
import java.util.Collections


class InThePastValidator extends AbstractKeyword("inThePast") {


  override def newValidator(schemaLocation: SchemaLocation, evaluationPath: JsonNodePath, schemaNode: JsonNode, parentSchema: JsonSchema, validationContext: ValidationContext): JsonValidator = {

    new AbstractJsonValidator(schemaLocation, evaluationPath, this,schemaNode) {
      override def validate(executionContext: ExecutionContext, node: JsonNode, rootNode: JsonNode, instanceLocation: JsonNodePath): util.Set[ValidationMessage] = {
        println("checking " + node + "is in the past")
        val set:util.HashSet[ValidationMessage] = new util.HashSet[ValidationMessage]
        val v = new ValidationMessage("checking " + node + "is in the past")
        set.add(v)
        set
      }
    }

  }
}
