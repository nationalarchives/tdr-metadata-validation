package uk.gov.nationalarchives.tdr.validation.schema

import com.networknt.schema._
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaDefinitions.BASE_SCHEMA
import uk.gov.nationalarchives.tdr.validation.schema.extensions.DaBeforeToday

import java.util
import scala.jdk.CollectionConverters._

object JsonSchemaValidators {

  private val validators: Map[JsonSchemaDefinitions, JsonSchema] = Map(BASE_SCHEMA -> baseJsonSchemaValidator)
  case class ValidationError(property:String,code:String)

  private lazy val baseJsonSchemaValidator: JsonSchema = {

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

  def validateJson(jsonSchemaDefinitions: JsonSchemaDefinitions, json: String): util.Set[ValidationError] = {
    val errors = validators(jsonSchemaDefinitions).validate(json, InputFormat.JSON)
    errors.asScala.map(covertValidationMessage).asJava
  }

  def covertValidationMessage(validationMessage: ValidationMessage) = {
     ValidationError(validationMessage.getInstanceLocation.getName(0),validationMessage.getMessageKey)
  }
}
