package uk.gov.nationalarchives.tdr.validation.schema

import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema._
import com.networknt.schema.dialect.Dialect
import com.networknt.schema.keyword.Keyword
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaDefinition.{BASE_SCHEMA, CLOSURE_SCHEMA_CLOSED, CLOSURE_SCHEMA_OPEN, RELATIONSHIP_SCHEMA, REQUIRED_SCHEMA}
import uk.gov.nationalarchives.tdr.validation.schema.extensions.{DaBeforeToday, MatchEndDateOrDateLastModified}

import scala.jdk.CollectionConverters._

object JsonSchemaValidators {

  private val mapper = new ObjectMapper()
  private val validators: Map[JsonSchemaDefinition, Schema] =
    Map(
      BASE_SCHEMA -> baseJsonSchemaValidator,
      CLOSURE_SCHEMA_CLOSED -> closureClosedJsonSchemaValidator,
      CLOSURE_SCHEMA_OPEN -> closureOpenJsonSchemaValidator,
      REQUIRED_SCHEMA -> requiredJsonSchemaValidator,
      RELATIONSHIP_SCHEMA -> relationshipJsonSchemaValidator
    )

  private lazy val baseJsonSchemaValidator: Schema =
    getJsonSchema(BASE_SCHEMA, Map("daBeforeToday" -> new DaBeforeToday, "matchEndDateOrDateLastModified" -> new MatchEndDateOrDateLastModified))
  private lazy val closureClosedJsonSchemaValidator: Schema = getJsonSchema(CLOSURE_SCHEMA_CLOSED)
  private lazy val closureOpenJsonSchemaValidator: Schema = getJsonSchema(CLOSURE_SCHEMA_OPEN)
  private lazy val requiredJsonSchemaValidator: Schema = getJsonSchema(REQUIRED_SCHEMA)
  private lazy val relationshipJsonSchemaValidator: Schema = getJsonSchema(RELATIONSHIP_SCHEMA)

  def validateJson(jsonSchemaDefinitions: JsonSchemaDefinition, json: String): Set[Error] = {
    validators(jsonSchemaDefinitions)
      .validate(json, InputFormat.JSON, (executionContext: ExecutionContext) =>
        executionContext.executionConfig((builder: ExecutionConfig.Builder) => builder.formatAssertionsEnabled(true))
      )
      .asScala
      .toSet
  }

  private def getJsonSchema(jsonSchemaDefinition: JsonSchemaDefinition, customSchemaKeywords: Map[String, Keyword] = Map.empty): Schema = {
    val schemaInputStream = getClass.getResourceAsStream(jsonSchemaDefinition.schemaLocation)

    val defaultDialect = Specification.getDialect(SpecificationVersion.DRAFT_2020_12)
    val dialectBuilder = Dialect.builder(defaultDialect)
    customSchemaKeywords.values.foreach(k => dialectBuilder.keyword(k))
    val dialect = dialectBuilder.build()

    val schemaRegistry = SchemaRegistry.withDefaultDialect(dialect)
    schemaRegistry.getSchema(schemaInputStream)
  }
}
