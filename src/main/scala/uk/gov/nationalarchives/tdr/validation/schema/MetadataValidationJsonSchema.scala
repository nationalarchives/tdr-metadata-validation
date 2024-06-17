package uk.gov.nationalarchives.tdr.validation.schema

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.networknt.schema.ValidationMessage
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.scaladsl.{Sink, Source}
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaDefinition.BASE_SCHEMA
import uk.gov.nationalarchives.tdr.validation.schema.JsonValidationErrorReason.BASE_SCHEMA_VALIDATION
import uk.gov.nationalarchives.tdr.validation.utils.CSVtoJsonUtils
import uk.gov.nationalarchives.tdr.validation.{Error, FileRow, Metadata}

import scala.concurrent.ExecutionContextExecutor

object MetadataValidationJsonSchema {

  case class ObjectMetadata(identifier: String, metadata: Set[Metadata])

  private case class ValidationErrors(jsonValidationErrorReason: JsonValidationErrorReason, identifier: String, errors: Set[ValidationMessage])

  private case class JsonData(identifier: String, json: String)

  implicit val system: ActorSystem = ActorSystem("MetadataValidation")
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  private val csvToJsonUtils = new CSVtoJsonUtils

  // Interface for draft metadata validator
  def validate(metadata: List[FileRow]): Map[String, List[Error]] = {
    val convertedFileRows: Seq[ObjectMetadata] = metadata.map(fileRow => ObjectMetadata(fileRow.fileName, fileRow.metadata.toSet))
    validate(BASE_SCHEMA, convertedFileRows.toSet)
  }

  /*
   Validate against specified schema
   */
  def validate(schemaDefinition: JsonSchemaDefinition, metadata: Set[ObjectMetadata]): Map[String, List[Error]] = {
    val validationProgram = for {
      validationErrors <- schemaValidation(schemaDefinition, metadata)
      errors <- convertSchemaValidatorError(validationErrors)
    } yield errors.toMap

    validationProgram.unsafeRunSync()
  }

  private def schemaValidation(schemaDefinition: JsonSchemaDefinition, metadata: Set[ObjectMetadata]): IO[Seq[ValidationErrors]] = {
    IO.fromFuture(
      IO(
        Source(metadata)
          .map(mapToJson)
          .map(validateWithSchema(schemaDefinition))
          .runWith(Sink.seq[ValidationErrors])
      )
    )
  }

  private def validateWithSchema(schemaDefinition: JsonSchemaDefinition) = { (jsonData: JsonData) =>
    schemaDefinition match {
      case BASE_SCHEMA =>
        val errors = JsonSchemaValidators.validateJson(schemaDefinition, jsonData.json)
        ValidationErrors(BASE_SCHEMA_VALIDATION, jsonData.identifier, errors)
    }
  }

  /*
   What we want to use for the errors has yet to be defined
   */
  private def convertSchemaValidatorError(errors: Seq[MetadataValidationJsonSchema.ValidationErrors]): IO[Seq[(String, List[Error])]] = {
    IO(errors.map(error => error.identifier -> error.errors.map(error => Error(error.getInstanceLocation.getName(0), error.getMessageKey)).toList))
  }

  private def mapToJson: ObjectMetadata => JsonData = (data: ObjectMetadata) => {
    val mapData = data.metadata.foldLeft(Map.empty[String, String])((acc, metadata) => acc + (metadata.name -> metadata.value))
    JsonData(data.identifier, csvToJsonUtils.convertToJSONString(mapData).replaceAll("\"\"", "null"))
  }
}
