package uk.gov.nationalarchives.tdr.validation.schema

import com.networknt.schema._
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaDefinition.{BASE_SCHEMA, CLOSURE_SCHEMA_CLOSED, CLOSURE_SCHEMA_OPEN}
import uk.gov.nationalarchives.tdr.validation.schema.extensions.DaBeforeToday

import scala.jdk.CollectionConverters._

object JsonSchemaValidators {

  private val validators: Map[JsonSchemaDefinition, JsonSchema] = Map(BASE_SCHEMA -> baseJsonSchemaValidator, CLOSURE_SCHEMA_CLOSED -> closureClosedJsonSchemaValidator, CLOSURE_SCHEMA_OPEN -> closureOpenJsonSchemaValidator)

  private lazy val baseJsonSchemaValidator: JsonSchema = getJsonSchema(BASE_SCHEMA, Map("daBeforeToday" -> new DaBeforeToday))
  private lazy val closureClosedJsonSchemaValidator: JsonSchema = getJsonSchema(CLOSURE_SCHEMA_CLOSED)
  private lazy val closureOpenJsonSchemaValidator: JsonSchema = getJsonSchema(CLOSURE_SCHEMA_OPEN)

  def validateJson(jsonSchemaDefinitions: JsonSchemaDefinition, json: String): Set[ValidationMessage] = {
    validators(jsonSchemaDefinitions).validate(json, InputFormat.JSON).asScala.toSet
  }

  private def getJsonSchema(jsonSchemaDefinition: JsonSchemaDefinition, customSchemaKeywords: Map[String, Keyword] = Map.empty): JsonSchema = {
    val schemaInputStream = getClass.getResourceAsStream(jsonSchemaDefinition.schemaLocation)
    val schema = JsonMetaSchema.getV202012

    schema.getKeywords.putAll(customSchemaKeywords.asJava)
    val jsonSchemaFactory = new JsonSchemaFactory.Builder()
      .defaultMetaSchemaIri(SchemaId.V202012)
      .metaSchema(JsonMetaSchema.getV202012)
      .build()

    val schemaValidatorsConfig = SchemaValidatorsConfig.builder().formatAssertionsEnabled(true).build()

    jsonSchemaFactory.getSchema(schemaInputStream, schemaValidatorsConfig)
  }
}
