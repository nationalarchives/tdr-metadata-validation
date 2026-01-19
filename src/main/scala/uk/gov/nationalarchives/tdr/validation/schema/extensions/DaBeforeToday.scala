package uk.gov.nationalarchives.tdr.validation.schema.extensions

import tools.jackson.databind.JsonNode
import com.networknt.schema._
import com.networknt.schema.keyword.{AbstractKeyword, AbstractKeywordValidator, KeywordValidator}
import com.networknt.schema.path.NodePath
import org.joda.time.DateTime

import java.util
import scala.collection.immutable.HashSet
import scala.jdk.CollectionConverters.SetHasAsJava
import scala.util.{Success, Try}

class DaBeforeToday extends AbstractKeyword("daBeforeToday") {

  override def newValidator(
      schemaLocation: SchemaLocation,
      schemaNode: JsonNode,
      schema: Schema,
      validationContext: SchemaContext
  ): KeywordValidator = {

    new AbstractKeywordValidator(this, schemaNode, schemaLocation) {
      override def validate(executionContext: ExecutionContext, node: JsonNode, rootNode: JsonNode, instanceLocation: NodePath): Unit = {
        val validationMessageBuilder = Error
          .builder()
          .instanceLocation(instanceLocation)
          .messageKey("daBeforeToday")

        Try(DateTime.parse(node.textValue())) match {
          case Success(date) if DateTime.now().isBefore(date) =>
            executionContext.addError(validationMessageBuilder.build())
          case _ =>
        }
      }
    }
  }
}
