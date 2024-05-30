package uk.gov.nationalarchives.tdr.validation.schema

sealed abstract class JsonValidationErrorReason(val reason: String)

object JsonValidationErrorReason {
  final case object BASE_SCHEMA_VALIDATION extends JsonValidationErrorReason("/schema/baseSchema.schema.json")
}