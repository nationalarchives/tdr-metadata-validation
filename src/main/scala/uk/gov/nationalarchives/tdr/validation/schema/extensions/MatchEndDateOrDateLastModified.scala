package uk.gov.nationalarchives.tdr.validation.schema.extensions

import com.networknt.schema._
import com.networknt.schema.keyword.{AbstractKeyword, AbstractKeywordValidator, KeywordValidator}
import com.networknt.schema.path.NodePath
import org.joda.time.DateTime
import tools.jackson.databind.JsonNode

import scala.util.{Success, Try}

class MatchEndDateOrDateLastModified extends AbstractKeyword("matchEndDateOrDateLastModified") {

  override def newValidator(
       schemaLocation: SchemaLocation,
       jsonNode: JsonNode,
       schema: Schema,
       schemaContext: SchemaContext
  ): KeywordValidator = {

    new AbstractKeywordValidator(this, jsonNode, schemaLocation) {
      override def validate(executionContext: ExecutionContext, node: JsonNode, rootNode: JsonNode, instanceLocation: NodePath): Unit = {
        val validationMessageBuilder = Error
          .builder()
          .instanceLocation(instanceLocation)
          .messageKey("matchEndDateOrDateLastModified")

        def parseDate(dateStr: String): Try[DateTime] = Try(DateTime.parse(dateStr))

        def isDec31(date: DateTime, year: Int): Boolean =
          date.getYear == year && date.getMonthOfYear == 12 && date.getDayOfMonth == 31

        def getValue(field: String): String = if (rootNode.has(field)) rootNode.get(field).asString() else ""

        def validateDate(closureStartDate: DateTime, dateToMatch: String): Boolean = {
          parseDate(dateToMatch) match {
            case Success(date) => closureStartDate == date || isDec31(closureStartDate, date.getYear)
            case _             => true
          }
        }

        parseDate(node.asString()) match {
          case Success(closureDate) =>
            val endDate = getValue("end_date")
            if (endDate != null && endDate.nonEmpty) {
              if (!validateDate(closureDate, endDate)) {
                executionContext.addError(validationMessageBuilder.build())
              }
            } else {
              if (!validateDate(closureDate, getValue("date_last_modified"))) {
                executionContext.addError(validationMessageBuilder.build())
              }
            }
          case _ =>
        }
      }
    }
  }
}
