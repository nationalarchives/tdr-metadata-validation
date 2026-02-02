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

sealed abstract class JsonSchemaDefinition(val schemaLocation: String, val validationProcess: ValidationProcess)

object JsonSchemaDefinition {
  case object BASE_SCHEMA extends JsonSchemaDefinition(mapToMetadataEnvironmentFile("/metadata-schema/baseSchema.schema.json"), SCHEMA_BASE)
  case object CLOSURE_SCHEMA_CLOSED extends JsonSchemaDefinition(mapToMetadataEnvironmentFile("/metadata-schema/closureSchemaClosed.schema.json"), SCHEMA_CLOSURE_CLOSED)
  case object CLOSURE_SCHEMA_OPEN extends JsonSchemaDefinition(mapToMetadataEnvironmentFile("/metadata-schema/closureSchemaOpen.schema.json"), SCHEMA_CLOSURE_OPEN)
  case object REQUIRED_SCHEMA extends JsonSchemaDefinition(mapToMetadataEnvironmentFile("/metadata-schema/requiredSchema.schema.json"), SCHEMA_REQUIRED)
  case object RELATIONSHIP_SCHEMA extends JsonSchemaDefinition(mapToMetadataEnvironmentFile("/metadata-schema/relationshipSchema.schema.json"), SCHEMA_RELATIONSHIP)
}
