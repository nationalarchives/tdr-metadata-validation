package uk.gov.nationalarchives.tdr.validation.schema

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.networknt.schema.ValidationMessage
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaDefinition.{BASE_SCHEMA, CLOSURE_SCHEMA}
import uk.gov.nationalarchives.tdr.validation.schema.JsonValidationErrorReason.{BASE_SCHEMA_VALIDATION, CLOSURE_SCHEMA_VALIDATION}
import uk.gov.nationalarchives.tdr.validation.utils.CSVtoJsonUtils
import uk.gov.nationalarchives.tdr.validation.{Error, FileRow, Metadata}

object MetadataValidationJsonSchema {

  case class ObjectMetadata(identifier: String, metadata: Set[Metadata])

  private case class ValidationErrors(jsonValidationErrorReason: JsonValidationErrorReason, identifier: String, errors: Set[ValidationMessage])

  private case class JsonData(identifier: String, json: String)

  private val csvToJsonUtils = new CSVtoJsonUtils

  // Interface for draft metadata validator
  def validate(metadata: List[FileRow]): Map[String, List[Error]] = {
    val convertedFileRows: Seq[ObjectMetadata] = metadata.map(fileRow => ObjectMetadata(fileRow.fileName, fileRow.metadata.toSet))
    val validationProgram = for {
      jsonData <- IO(convertedFileRows.map(objectMetadata => mapToJson(objectMetadata)))
      validationErrors <- IO(jsonData.map(jsonData => validateWithSchema(BASE_SCHEMA)(jsonData)))
      closureValidationErrors <- IO(jsonData.map(jsonData => validateWithSchema(CLOSURE_SCHEMA)(jsonData)))
      errors <- convertSchemaValidatorError(validationErrors ++ closureValidationErrors)
    } yield errors.toMap

    validationProgram.unsafeRunSync()
  }

  /*
   Validate against specified schema
   */
  def validate(schemaDefinition: JsonSchemaDefinition, metadata: Set[ObjectMetadata]): Map[String, List[Error]] = {
    val validationProgram = for {
      jsonData <- IO(metadata.map(objectMetadata => mapToJson(objectMetadata)))
      validationErrors <- IO(jsonData.map(jsonData => validateWithSchema(schemaDefinition)(jsonData)))
      errors <- convertSchemaValidatorError(validationErrors.toSeq)
    } yield errors.toMap

    validationProgram.unsafeRunSync()
  }

  private def validateWithSchema(schemaDefinition: JsonSchemaDefinition): JsonData => ValidationErrors = { (jsonData: JsonData) =>
    schemaDefinition match {
      case BASE_SCHEMA =>
        val errors = JsonSchemaValidators.validateJson(schemaDefinition, jsonData.json)
        ValidationErrors(BASE_SCHEMA_VALIDATION, jsonData.identifier, errors)
      case CLOSURE_SCHEMA =>
        val errors = JsonSchemaValidators.validateJson(schemaDefinition, jsonData.json)
        ValidationErrors(CLOSURE_SCHEMA_VALIDATION, jsonData.identifier, errors)
    }
  }

  /*
   What we want to use for the errors has yet to be defined
   */
  private def convertSchemaValidatorError(errors: Seq[MetadataValidationJsonSchema.ValidationErrors]): IO[Seq[(String, List[Error])]] = {
    IO(errors.map(error => error.identifier -> error.errors.map(convertValidationMessageToError).toList))
  }

  private def convertValidationMessageToError(message: ValidationMessage): Error = {
    Error(Option(message.getProperty).getOrElse(message.getInstanceLocation.getName(0)), message.getMessageKey)
  }

  private def mapToJson: ObjectMetadata => JsonData = (data: ObjectMetadata) => {
    val mapData = data.metadata.foldLeft(Map.empty[String, String])((acc, metadata) => acc + (metadata.name -> metadata.value))
    JsonData(data.identifier, csvToJsonUtils.convertToJSONString(mapData).replaceAll("\"\"", "null"))
  }
}
