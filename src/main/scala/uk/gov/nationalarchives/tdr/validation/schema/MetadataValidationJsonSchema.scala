package uk.gov.nationalarchives.tdr.validation.schema

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.all._
import com.networknt.schema.ValidationMessage
import uk.gov.nationalarchives.tdr.validation.schema.ValidationProcess._
import uk.gov.nationalarchives.tdr.validation.utils.CSVtoJsonUtils
import uk.gov.nationalarchives.tdr.validation.{FileRow, Metadata}

case class ValidationError(validationProcess: ValidationProcess, property: String, errorKey: String)

object MetadataValidationJsonSchema {

  case class ObjectMetadata(identifier: String, metadata: Set[Metadata])

  private case class ValidationErrorWithValidationMessages(jsonValidationErrorReason: ValidationProcess, identifier: String, errors: Set[ValidationMessage])

  private case class JsonData(identifier: String, json: String)

  private val csvToJsonUtils = new CSVtoJsonUtils

  def validate(schema: Set[JsonSchemaDefinition], metadata: Seq[FileRow]): Map[String, Seq[ValidationError]] = {
    val convertedFileRows: Seq[ObjectMetadata] = metadata.map(fileRow => ObjectMetadata(fileRow.matchIdentifier, fileRow.metadata.toSet))

    val validationProgram = for {
      jsonData <- IO(convertedFileRows.map(objectMetadata => mapToJson(objectMetadata)))
      schemaValidationErrors <- parallelSchemaValidation(schema, jsonData)
      errors <- convertSchemaValidatorError(schemaValidationErrors.flatten)
      combinedErrors <- combineErrors(errors)
    } yield combinedErrors

    validationProgram.unsafeRunSync()
  }

  private def parallelSchemaValidation(schema: Set[JsonSchemaDefinition], jsonData: Seq[JsonData]): IO[Seq[Seq[ValidationErrorWithValidationMessages]]] = {
    val validations: Seq[IO[Seq[ValidationErrorWithValidationMessages]]] =
      schema.toSeq.map(schemaDefinition => IO(jsonData.map(json => validateWithSchema(schemaDefinition)(json))))
    validations.parSequence
  }

  /*
   Validate against specified schema
   */
  def validateWithSingleSchema(schemaDefinition: JsonSchemaDefinition, metadata: Set[ObjectMetadata]): Map[String, List[ValidationError]] = {
    val validationProgram = for {
      jsonData <- IO(metadata.map(objectMetadata => mapToJson(objectMetadata)))
      validationErrors <- IO(jsonData.map(jsonData => validateWithSchema(schemaDefinition)(jsonData)))
      errors <- convertSchemaValidatorError(validationErrors.toList)
    } yield errors.toMap

    validationProgram.unsafeRunSync()
  }

  private def validateWithSchema(schemaDefinition: JsonSchemaDefinition): JsonData => ValidationErrorWithValidationMessages = { (jsonData: JsonData) =>
    val errors = JsonSchemaValidators.validateJson(schemaDefinition, jsonData.json)
    ValidationErrorWithValidationMessages(schemaDefinition.validationProcess, jsonData.identifier, errors)
  }

  /*
   What we want to use for the errors has yet to be defined
   */
  private def convertSchemaValidatorError(errors: Seq[ValidationErrorWithValidationMessages]): IO[Seq[(String, List[ValidationError])]] = {
    IO(errors.map(error => error.identifier -> error.errors.map(validationMessage => convertValidationMessageToError(validationMessage, error.jsonValidationErrorReason)).toList))
  }

  private def combineErrors(errors: Seq[(String, List[ValidationError])]) = {
    IO(errors.foldLeft(Map.empty[String, List[ValidationError]]) { case (idToErrorMap, (id, errors)) =>
      idToErrorMap.updated(id, idToErrorMap.getOrElse(id, List.empty[ValidationError]) ++ errors)
    })
  }

  private def convertValidationMessageToError(message: ValidationMessage, validationProcess: ValidationProcess): ValidationError = {
    val propertyName = Option(message.getProperty)
      .map(_.replaceAll("\\[\\d+]$", ""))
      .getOrElse(message.getInstanceLocation.getName(0))
    ValidationError(validationProcess, propertyName, message.getMessageKey)
  }

  private def mapToJson: ObjectMetadata => JsonData = (data: ObjectMetadata) => {
    val mapData = data.metadata.foldLeft(Map.empty[String, String])((acc, metadata) => acc + (metadata.name -> metadata.value))
    JsonData(data.identifier, csvToJsonUtils.convertToJSONString(mapData).replaceAll("\"\"", "null"))
  }
}
