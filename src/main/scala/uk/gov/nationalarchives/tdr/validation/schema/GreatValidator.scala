package uk.gov.nationalarchives.tdr.validation.schema

import com.fasterxml.jackson.databind.JsonNode
import com.networknt.schema.{AbstractJsonValidator, AbstractKeyword, ExecutionContext, JsonNodePath, JsonSchema, JsonValidator, SchemaLocation, ValidationContext, ValidationMessage}

import java.util
import java.util.Collections


class GreatValidator extends AbstractKeyword("great") {


  override def newValidator(schemaLocation: SchemaLocation, evaluationPath: JsonNodePath, schemaNode: JsonNode, parentSchema: JsonSchema, validationContext: ValidationContext): JsonValidator = {

     val config = schemaNode.asText
    new AbstractJsonValidator(schemaLocation, evaluationPath, this,schemaNode) {

      override def validate(executionContext: ExecutionContext, node: JsonNode, rootNode: JsonNode, instanceLocation: JsonNodePath): util.Set[ValidationMessage] = {
        println("Whey hey")
        Collections.emptySet
      }
    }

  }
}
