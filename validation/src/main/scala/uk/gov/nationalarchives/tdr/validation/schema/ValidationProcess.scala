package uk.gov.nationalarchives.tdr.validation.schema

object ValidationProcess extends Enumeration {
  type ValidationProcess = Value
  val SCHEMA_BASE, SCHEMA_CLOSURE_CLOSED, SCHEMA_CLOSURE_OPEN = Value
}
