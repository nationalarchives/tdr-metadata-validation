package uk.gov.nationalarchives.tdr.validation.schema

import uk.gov.nationalarchives.tdr.validation.schema.ValidationProcess.{SCHEMA_BASE, SCHEMA_CLOSURE_CLOSED, SCHEMA_CLOSURE_OPEN, SCHEMA_REQUIRED, ValidationProcess}

sealed abstract class JsonSchemaDefinition(val schemaLocation: String, val validationProcess: ValidationProcess)

object JsonSchemaDefinition {
  final case object BASE_SCHEMA extends JsonSchemaDefinition("/metadata-schema/baseSchema.schema.json", SCHEMA_BASE)
  final case object CLOSURE_SCHEMA_CLOSED extends JsonSchemaDefinition("/metadata-schema/closureSchemaClosed.schema.json", SCHEMA_CLOSURE_CLOSED)
  final case object CLOSURE_SCHEMA_OPEN extends JsonSchemaDefinition("/metadata-schema/closureSchemaOpen.schema.json", SCHEMA_CLOSURE_OPEN)
  final case object REQUIRED_SCHEMA extends JsonSchemaDefinition("/metadata-schema/requiredSchema.schema.json", SCHEMA_REQUIRED)
}
