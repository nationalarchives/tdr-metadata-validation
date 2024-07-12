package uk.gov.nationalarchives.tdr.validation.schema

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.networknt.schema.ValidationMessage
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaDefinition.{BASE_SCHEMA, CLOSURE_SCHEMA, DATA_LOAD_SHAREPOINT_SCHEMA}
import uk.gov.nationalarchives.tdr.validation.schema.JsonValidationErrorReason.{BASE_SCHEMA_VALIDATION, CLOSURE_SCHEMA_VALIDATION, DATA_LOAD_SHAREPOINT_SCHEMA_VALIDATION}
import uk.gov.nationalarchives.tdr.validation.utils.CSVtoJsonUtils
import uk.gov.nationalarchives.tdr.validation.{Error, FileRow, Metadata}

object MetadataValidationJsonSchema {

  case class ObjectMetadata(identifier: String, metadata: Set[Metadata])

  case class SchemaValidationResults(schemaLocation: String, schemaErrors: Map[String, List[Error]])

  private case class ValidationErrors(jsonValidationErrorReason: JsonValidationErrorReason, identifier: String, errors: Set[ValidationMessage])

  private case class JsonData(identifier: String, json: String)

  private val csvToJsonUtils = new CSVtoJsonUtils

  private val defaultAlternativeHeaderKey = "tdrFileHeader"

  // Interface for draft metadata validator
  def validate(metadata: List[FileRow]): Map[String, List[Error]] = {
    val convertedFileRows: Seq[ObjectMetadata] = metadata.map(fileRow => ObjectMetadata(fileRow.fileName, fileRow.metadata.toSet))
    val validationProgram = for {
      jsonData <- IO(convertedFileRows.map(objectMetadata => mapToJson(Some(defaultAlternativeHeaderKey))(objectMetadata)))
      validationErrors <- IO(jsonData.map(jsonData => validateWithSchema(BASE_SCHEMA)(jsonData)))
      closureValidationErrors <- IO(jsonData.map(jsonData => validateWithSchema(CLOSURE_SCHEMA)(jsonData)))
      errors <- convertSchemaValidatorError(validationErrors ++ closureValidationErrors)
    } yield errors.toMap

    validationProgram.unsafeRunSync()
  }

  /*
   Validate against multiple schemas
   */
  def validate(
      schemaDefinitions: Set[JsonSchemaDefinition],
      metadata: Set[ObjectMetadata],
      alternativeHeaderKey: Option[String] = None
  ): Set[SchemaValidationResults] = {
    val validationPrograms = schemaDefinitions.map(schema =>
      for {
        jsonData <- IO(metadata.map(objectMetadata => mapToJson(alternativeHeaderKey)(objectMetadata)))
        validationErrors <- IO(jsonData.map(jsonData => validateWithSchema(schema)(jsonData)))
        headersMapping = alternativeHeaderKey match {
          case Some(key) => Some(csvToJsonUtils.propertyNameToAlternativeKeyMapping(key))
          case _         => None
        }
        errors <- convertSchemaValidatorError(validationErrors.toSeq, headersMapping)
      } yield SchemaValidationResults(schema.location, errors.toMap)
    )

    validationPrograms.map(_.unsafeRunSync())
  }

  private def validateWithSchema(schemaDefinition: JsonSchemaDefinition): JsonData => ValidationErrors = { (jsonData: JsonData) =>
    schemaDefinition match {
      case BASE_SCHEMA =>
        val errors = JsonSchemaValidators.validateJson(schemaDefinition, jsonData.json)
        ValidationErrors(BASE_SCHEMA_VALIDATION, jsonData.identifier, errors)
      case CLOSURE_SCHEMA =>
        val errors = JsonSchemaValidators.validateJson(schemaDefinition, jsonData.json)
        ValidationErrors(CLOSURE_SCHEMA_VALIDATION, jsonData.identifier, errors)
      case DATA_LOAD_SHAREPOINT_SCHEMA =>
        val errors = JsonSchemaValidators.validateJson(schemaDefinition, jsonData.json)
        ValidationErrors(DATA_LOAD_SHAREPOINT_SCHEMA_VALIDATION, jsonData.identifier, errors)
    }
  }

  /*
   What we want to use for the errors has yet to be defined
   */
  private def convertSchemaValidatorError(
      errors: Seq[MetadataValidationJsonSchema.ValidationErrors],
      headersMapping: Option[Map[String, String]] = None
  ): IO[Seq[(String, List[Error])]] = {
    IO(errors.map(error => error.identifier -> error.errors.map(convertValidationMessageToError(_, headersMapping)).toList))
  }

  private def convertValidationMessageToError(message: ValidationMessage, headersMapping: Option[Map[String, String]]): Error = {

    val messagePropertyName = message.getProperty
    val propertyName = headersMapping match {
      case Some(mapping) => mapping.getOrElse(messagePropertyName, messagePropertyName)
      case _             => messagePropertyName
    }

    Error(Option(propertyName).getOrElse(message.getInstanceLocation.getName(0)), message.getMessageKey)
  }

  private def mapToJson(alternativeKey: Option[String]): ObjectMetadata => JsonData = (data: ObjectMetadata) => {
    val mapData = data.metadata.foldLeft(Map.empty[String, String])((acc, metadata) => acc + (metadata.name -> metadata.value))
    JsonData(data.identifier, csvToJsonUtils.convertToJSONString(mapData, alternativeKey).replaceAll("\"\"", "null"))
  }
}
