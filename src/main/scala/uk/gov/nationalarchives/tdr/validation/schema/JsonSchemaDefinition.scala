package uk.gov.nationalarchives.tdr.validation.schema

sealed abstract class JsonSchemaDefinition(val location: String)

object JsonSchemaDefinition {
  final case object BASE_SCHEMA extends JsonSchemaDefinition("/metadata-schema/baseSchema.schema.json")
  final case object CLOSURE_SCHEMA extends JsonSchemaDefinition("/metadata-schema/closureSchema.schema.json")
}
