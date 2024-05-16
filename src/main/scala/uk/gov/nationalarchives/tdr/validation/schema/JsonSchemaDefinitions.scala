package uk.gov.nationalarchives.tdr.validation.schema

sealed abstract class JsonSchemaDefinitions(val location: String)

object JsonSchemaDefinitions {
    final case object BASE_SCHEMA extends JsonSchemaDefinitions("/schema/baseSchema.schema.json")
}
