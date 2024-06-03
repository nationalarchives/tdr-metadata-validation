package uk.gov.nationalarchives.tdr.validation.schema

sealed abstract class JsonSchemaDefinition(val location: String)

object JsonSchemaDefinition {
  final case object BASE_SCHEMA extends JsonSchemaDefinition("/baseSchema.schema.json")
}