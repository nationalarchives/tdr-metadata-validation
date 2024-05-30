package uk.gov.nationalarchives.tdr.validation.schema

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.networknt.schema.ValidationMessage
import org.apache.pekko.NotUsed
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.scaladsl.{Flow, Sink, Source}
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaDefinition.BASE_SCHEMA
import uk.gov.nationalarchives.tdr.validation.schema.JsonValidationErrorReason.BASE_SCHEMA_VALIDATION
import uk.gov.nationalarchives.tdr.validation.{Error, Metadata}

import scala.concurrent.ExecutionContextExecutor

object MetadataValidationJsonSchema {
  implicit val system: ActorSystem = ActorSystem("QuickStart")
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  case class ObjectMetadata(identifier: String, metadata: Set[Metadata])
  case class ValidationStreamData(schemaDefinition: JsonSchemaDefinition, metadata: ObjectMetadata)
  case class ValidationErrors(jsonValidationErrorReason: JsonValidationErrorReason, identifier: String, errors: Set[ValidationMessage])

  private case class JsonData(schemaDefinition: JsonSchemaDefinition, identifier: String, jsonData: String)

  def validate(schemaDefinition: JsonSchemaDefinition, metadata: Set[ObjectMetadata]): Map[String, List[Error]] = {

    val validationProgram =  for {
      validationErrors <- streamValidation(schemaDefinition, metadata)
      userPrettyErrors <- processErrors(validationErrors)
    } yield { userPrettyErrors.toMap }

    validationProgram.unsafeRunSync()

  }

  private def streamValidation(schemaDefinition: JsonSchemaDefinition, metadata: Set[ObjectMetadata]): IO[Seq[ValidationErrors]] = {
    IO.fromFuture(
      IO(
        Source(metadata.map(metadata => ValidationStreamData(schemaDefinition, metadata)))
          .via(validationFlow)
          .runWith(Sink.seq[ValidationErrors])
      )
    )
  }

  def validationFlow: Flow[ValidationStreamData, ValidationErrors, NotUsed] = {
    Flow[ValidationStreamData]
      .map(mapToJson)
      .map(validateWithSchema)

  }

  private def validateWithSchema: JsonData => ValidationErrors = (data: JsonData) => {
    data.schemaDefinition match {
      case BASE_SCHEMA =>
        val errors = JsonSchemaValidators.validateJson(data.schemaDefinition, data.jsonData)
        ValidationErrors(BASE_SCHEMA_VALIDATION, data.identifier, errors)
    }
  }

  /*
   What we want to use for the errors has yet to be defined
   */
  private def processErrors(errors: Seq[MetadataValidationJsonSchema.ValidationErrors]): IO[Seq[(String, List[Error])]] = {
    IO(errors.map(error => error.identifier -> error.errors.map(error => Error(error.getInstanceLocation.getName(0), error.getMessage)).toList))
  }

  private def mapToJson = (data: ValidationStreamData) => {
    val json = """
    {
        "description" : "hello",
        "date_last_modified" : "2044-06-30T01:20+02:00"
    }""".stripMargin
    JsonData(data.schemaDefinition, data.metadata.identifier, json)
  }

}
