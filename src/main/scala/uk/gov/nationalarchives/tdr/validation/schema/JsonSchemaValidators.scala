package uk.gov.nationalarchives.tdr.validation.schema

import com.networknt.schema._
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaDefinition.BASE_SCHEMA
import uk.gov.nationalarchives.tdr.validation.schema.extensions.DaBeforeToday

import scala.jdk.CollectionConverters._

object JsonSchemaValidators {

  private val validators: Map[JsonSchemaDefinition, JsonSchema] = Map(BASE_SCHEMA -> baseJsonSchemaValidator)

  private lazy val baseJsonSchemaValidator: JsonSchema = {

    val schemaInputStream = getClass.getResourceAsStream(BASE_SCHEMA.location)
    val schema = JsonMetaSchema.getV7

    schema.getKeywords.put("daBeforeToday", new DaBeforeToday)
    val jsonSchemaFactory = new JsonSchemaFactory.Builder()
      .defaultMetaSchemaIri(SchemaId.V7)
      .metaSchema(schema)
      .build()

    val schemaValidatorsConfig = new SchemaValidatorsConfig()
    schemaValidatorsConfig.setFormatAssertionsEnabled(true)

    jsonSchemaFactory.getSchema(schemaInputStream, schemaValidatorsConfig)
  }

  def validateJson(jsonSchemaDefinitions: JsonSchemaDefinition, json: String): Set[ValidationMessage] = {
    validators(jsonSchemaDefinitions).validate(json, InputFormat.JSON).asScala.toSet
  }
}
