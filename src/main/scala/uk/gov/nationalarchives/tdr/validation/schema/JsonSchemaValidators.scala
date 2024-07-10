package uk.gov.nationalarchives.tdr.validation.schema

import com.networknt.schema._
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaDefinition.{BASE_SCHEMA, CLOSURE_SCHEMA, DATA_LOAD_SHAREPOINT_SCHEMA}
import uk.gov.nationalarchives.tdr.validation.schema.extensions.DaBeforeToday

import scala.jdk.CollectionConverters._

object JsonSchemaValidators {

  private val validators: Map[JsonSchemaDefinition, JsonSchema] = Map(
    BASE_SCHEMA -> baseJsonSchemaValidator,
    CLOSURE_SCHEMA -> closureJsonSchemaValidator,
    DATA_LOAD_SHAREPOINT_SCHEMA -> dataLoadSharePointJsonSchemaValidator
  )

  private lazy val baseJsonSchemaValidator: JsonSchema = getJsonSchema(BASE_SCHEMA, Map("daBeforeToday" -> new DaBeforeToday))
  private lazy val closureJsonSchemaValidator: JsonSchema = getJsonSchema(CLOSURE_SCHEMA)
  private lazy val dataLoadSharePointJsonSchemaValidator: JsonSchema = getJsonSchema(DATA_LOAD_SHAREPOINT_SCHEMA)

  def validateJson(jsonSchemaDefinitions: JsonSchemaDefinition, json: String): Set[ValidationMessage] = {
    validators(jsonSchemaDefinitions).validate(json, InputFormat.JSON).asScala.toSet
  }

  private def getJsonSchema(jsonSchemaDefinition: JsonSchemaDefinition, customSchemaKeywords: Map[String, Keyword] = Map.empty): JsonSchema = {
    val schemaInputStream = getClass.getResourceAsStream(jsonSchemaDefinition.location)
    val schema = JsonMetaSchema.getV7

    schema.getKeywords.putAll(customSchemaKeywords.asJava)
    val jsonSchemaFactory = new JsonSchemaFactory.Builder()
      .defaultMetaSchemaIri(SchemaId.V7)
      .metaSchema(schema)
      .build()

    val schemaValidatorsConfig = SchemaValidatorsConfig
      .builder()
      .formatAssertionsEnabled(true)
      .build()

    jsonSchemaFactory.getSchema(schemaInputStream, schemaValidatorsConfig)
  }
}
