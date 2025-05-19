package uk.gov.nationalarchives.tdr.validation.schema.extensions

import com.fasterxml.jackson.databind.JsonNode
import com.networknt.schema._
import org.joda.time.DateTime

import java.util
import scala.collection.immutable.HashSet
import scala.jdk.CollectionConverters.SetHasAsJava
import scala.util.{Success, Try}

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
          .message("daBeforeToday")
          .messageKey("daBeforeToday")

        val validationMessages = Try(DateTime.parse(node.textValue())) match {
          case Success(date) if DateTime.now().isBefore(date) => HashSet(validationMessageBuilder.build())
          case _                                             => HashSet[ValidationMessage]()
        }
        validationMessages.asJava
      }
    }
  }
}
