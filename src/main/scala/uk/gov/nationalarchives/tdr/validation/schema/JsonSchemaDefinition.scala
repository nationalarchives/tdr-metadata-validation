package uk.gov.nationalarchives.tdr.validation.schema

import uk.gov.nationalarchives.tdr.validation.schema.ValidationProcess.{SCHEMA_BASE, SCHEMA_CLOSURE, ValidationProcess}

sealed abstract class JsonSchemaDefinition(val schemaLocation: String, val validationProcess: ValidationProcess)

object JsonSchemaDefinition {
  final case object BASE_SCHEMA extends JsonSchemaDefinition("/metadata-schema/baseSchema.schema.json", SCHEMA_BASE)
  final case object CLOSURE_SCHEMA extends JsonSchemaDefinition("/metadata-schema/closureSchema.schema.json", SCHEMA_CLOSURE)
}
