package uk.gov.nationalarchives.tdr.validation.schema.extensions

import com.fasterxml.jackson.databind.JsonNode
import com.networknt.schema._
import org.joda.time.DateTime

import java.util
import java.util.Collections
import scala.util.{Failure, Success, Try}

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
        val validationMessageBuilder = ValidationMessage.builder().instanceLocation(instanceLocation)
           .message("daBeforeToday")
           .messageKey("daBeforeToday")
        val errors = new util.HashSet[ValidationMessage]()
        val date = Try( DateTime.parse(node.textValue))
        date match {
          case Failure(_) => println("OHHHHHHHHHHHHHHHHHHHHHHHHH")
          case Success(value) => if (DateTime.now() isBefore value) errors.add(validationMessageBuilder.build())
        }
        errors
      }
    }
  }
}

