package uk.gov.nationalarchives.tdr.validation.schema

import com.networknt.schema.{InputFormat, JsonMetaSchema, JsonSchema, JsonSchemaFactory, SchemaId, SchemaValidatorsConfig, ValidationMessage}
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaDefinitions.BASE_SCHEMA
import uk.gov.nationalarchives.tdr.validation.schema.extensions.DaBeforeToday

import java.util
import scala.jdk.CollectionConverters.CollectionHasAsScala

object JsonSchemaValidators {

  private val validators: Map[JsonSchemaDefinition, JsonSchema] = Map(BASE_SCHEMA -> baseJsonSchemaValidator)

  private lazy val baseJsonSchemaValidator: JsonSchema = {

    println("building validator")
    val schemaInputStream = getClass.getResourceAsStream(BASE_SCHEMA.location)
    val sch = JsonMetaSchema.getV7
    sch.getKeywords.put("daBeforeToday", new DaBeforeToday)
    val factory1 = new JsonSchemaFactory.Builder()
      .defaultMetaSchemaIri(SchemaId.V7)
      .metaSchema(sch)
      .build()

    val config = new SchemaValidatorsConfig()
    config.setFormatAssertionsEnabled(true)

    factory1.getSchema(schemaInputStream, config)
  }

  def validateJson(jsonSchemaDefinition: JsonSchemaDefinition, json: String): Set[ValidationMessage] = {

    validators(jsonSchemaDefinition).validate(json, InputFormat.JSON).asScala.toSet
  }
}
