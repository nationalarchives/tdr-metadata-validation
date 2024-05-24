package uk.gov.nationalarchives.tdr.validation.schema

sealed abstract class JsonSchemaDefinition(val location: String)

object JsonSchemaDefinitions {
  final case object BASE_SCHEMA extends JsonSchemaDefinition("/schema/baseSchema.schema.json")
}
