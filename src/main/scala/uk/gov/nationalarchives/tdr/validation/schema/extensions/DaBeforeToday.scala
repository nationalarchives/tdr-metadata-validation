package uk.gov.nationalarchives.tdr.validation.schema.extensions

import com.fasterxml.jackson.databind.JsonNode
import com.networknt.schema._

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
        val validationMessageBuilder = ValidationMessage
          .builder()
          .instanceLocation(instanceLocation)
          .message("FUTURE_DATE_ERROR")
        val errors = new util.HashSet[ValidationMessage]()
        errors.add(validationMessageBuilder.build())
        errors
      }
    }
  }
}
