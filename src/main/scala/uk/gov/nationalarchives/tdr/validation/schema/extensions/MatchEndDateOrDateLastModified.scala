package uk.gov.nationalarchives.tdr.validation.schema.extensions

import com.fasterxml.jackson.databind.JsonNode
import com.networknt.schema._
import org.joda.time.DateTime

import java.util
import scala.collection.immutable.HashSet
import scala.jdk.CollectionConverters.SetHasAsJava
import scala.util.{Success, Try}

class MatchEndDateOrDateLastModified extends AbstractKeyword("matchEndDateOrDateLastModified") {

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
          .message("matchEndDateOrDateLastModified")
          .messageKey("matchEndDateOrDateLastModified")

        def parseDate(dateStr: String): Try[DateTime] = Try(DateTime.parse(dateStr))

        def isDec31(date: DateTime, year: Int): Boolean =
          date.getYear == year && date.getMonthOfYear == 12 && date.getDayOfMonth == 31

        def getValue(field: String): String = if (rootNode.has(field)) rootNode.get(field).textValue() else ""

        def validateDate(closureStartDate: DateTime, dateToMatch: String): Boolean = {
          parseDate(dateToMatch) match {
            case Success(date) => closureStartDate == date || isDec31(closureStartDate, date.getYear)
            case _             => true
          }
        }

        val validationMessages = parseDate(node.textValue()) match {
          case Success(closureDate) =>
            if (getValue("end_date") != null) {
              if (validateDate(closureDate, getValue("end_date"))) {
                HashSet[ValidationMessage]()
              } else {
                HashSet(validationMessageBuilder.build())
              }
            } else {
              if (validateDate(closureDate, getValue("date_last_modified"))) {
                HashSet[ValidationMessage]()
              } else {
                HashSet(validationMessageBuilder.build())
              }
            }
          case _ => HashSet[ValidationMessage]()
        }
        validationMessages.asJava
      }
    }
  }
}
