package uk.gov.nationalarchives.tdr.validation.jsonschema.extensions

import com.fasterxml.jackson.databind.JsonNode
import com.networknt.schema.{
  AbstractJsonValidator,
  AbstractKeyword,
  ExecutionContext,
  JsonNodePath,
  JsonSchema,
  JsonValidator,
  SchemaLocation,
  ValidationContext,
  ValidationMessage
}

import java.util
import java.util.Collections

class DaBeforeToday extends AbstractKeyword("daBeforeToday") {

  override def newValidator(
      schemaLocation: SchemaLocation,
      evaluationPath: JsonNodePath,
      schemaNode: JsonNode,
      parentSchema: JsonSchema,
      validationContext: ValidationContext
  ): JsonValidator = {

    new AbstractJsonValidator(schemaLocation, evaluationPath, this, schemaNode) {
      override def validate(executionContext: ExecutionContext, node: JsonNode, rootNode: JsonNode, instanceLocation: JsonNodePath): util.Set[ValidationMessage] = {
        println("checking " + node + "is in the past")
        val validationMessageBuilder = ValidationMessage.builder()
        validationMessageBuilder.message("ERROR_XXXXXXXXXXXXXX")
        val Errors = Collections.emptySet[ValidationMessage]
        Errors.add(validationMessageBuilder.build())
        Errors
      }
    }
  }
}
