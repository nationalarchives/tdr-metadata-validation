package uk.gov.nationalarchives.tdr.validation.schema

sealed abstract class JsonValidationErrorReason(val reason: String)

object JsonValidationErrorReason {
  final case object BASE_SCHEMA_VALIDATION extends JsonValidationErrorReason("/schema/baseSchema.schema.json")
  final case object CLOSURE_SCHEMA_VALIDATION extends JsonValidationErrorReason("/schema/closureSchema.schema.json")
  final case object DATA_LOAD_SHAREPOINT_SCHEMA_VALIDATION extends JsonValidationErrorReason("/schema/dataLoadSharePointSchema.schema.json")
}
