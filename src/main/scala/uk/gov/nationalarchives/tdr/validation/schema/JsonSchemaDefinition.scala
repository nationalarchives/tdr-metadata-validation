package uk.gov.nationalarchives.tdr.validation.schema

import uk.gov.nationalarchives.tdr.schemautils.ConfigUtils.mapToMetadataEnvironmentFile
import uk.gov.nationalarchives.tdr.validation.schema.ValidationProcess.{
  SCHEMA_BASE,
  SCHEMA_CLOSURE_CLOSED,
  SCHEMA_CLOSURE_OPEN,
  SCHEMA_REQUIRED,
  SCHEMA_RELATIONSHIP,
  ValidationProcess
}

sealed abstract class JsonSchemaDefinition(schemaPath: String, val validationProcess: ValidationProcess) {
  val schemaLocation: String = mapToMetadataEnvironmentFile(schemaPath)
}

object JsonSchemaDefinition {
  case object BASE_SCHEMA extends JsonSchemaDefinition("/metadata-schema/baseSchema.schema.json", SCHEMA_BASE)
  case object CLOSURE_SCHEMA_CLOSED extends JsonSchemaDefinition("/metadata-schema/closureSchemaClosed.schema.json", SCHEMA_CLOSURE_CLOSED)
  case object CLOSURE_SCHEMA_OPEN extends JsonSchemaDefinition("/metadata-schema/closureSchemaOpen.schema.json", SCHEMA_CLOSURE_OPEN)
  case object REQUIRED_SCHEMA extends JsonSchemaDefinition("/metadata-schema/requiredSchema.schema.json", SCHEMA_REQUIRED)
  case object RELATIONSHIP_SCHEMA extends JsonSchemaDefinition("/metadata-schema/relationshipSchema.schema.json", SCHEMA_RELATIONSHIP)
}
