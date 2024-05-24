package uk.gov.nationalarchives.tdr.validation.schema

import com.networknt.schema.ValidationMessage
import org.apache.pekko.NotUsed
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.scaladsl.{Flow, Sink, Source}
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaDefinitions.BASE_SCHEMA
import uk.gov.nationalarchives.tdr.validation.schema.MetadataValidationJsonSchema.JsonValidationErrorReasons.BASE_SCHEMA_VALIDATION
import uk.gov.nationalarchives.tdr.validation.{Error, Metadata}

import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.{Failure, Success, Try}

object MetadataValidationJsonSchema {

  case class ObjectMetadata(identifier: String, metadata: Set[Metadata])
  case class ValidationStreamData(schemaDefinition: JsonSchemaDefinition, metadata: ObjectMetadata)
  private case class JsonData(schemaDefinition: JsonSchemaDefinition, identifier: String, jsonData: String)
  private case class ValidationErrors(jsonValidationErrorReason: JsonValidationErrorReason, identifier: String, errors: Set[ValidationMessage])

  def validate(schemaDefinition: JsonSchemaDefinition, metadata: Set[ObjectMetadata]): Map[String, List[Error]] = {
    implicit val system: ActorSystem = ActorSystem("QuickStart")
    implicit val ec: ExecutionContextExecutor = system.dispatcher

    val streamData = metadata.map(metadata => ValidationStreamData(schemaDefinition, metadata))
    val execution: Future[Seq[(String, Set[Error])]] = Source(streamData).via(validationFlow).runWith(Sink.f.seq[(String, Set[Error])])

    val result: Try[Seq[(String, Set[Error])]] = Await.ready(execution, Duration.Inf).value.get

    val resultEither = result match {
      case Success(t) => t
      case Failure(_) => Set[(String, Set[Error])]()
    }
    val accumulatorMap = scala.collection.mutable.Map.empty[String, List[Error]]
    resultEither.foldLeft(accumulatorMap)((acc, x) => acc += x._1 -> x._2.toList).toMap

  }

  def validationFlow: Flow[ValidationStreamData, (String, Set[Error]), NotUsed] = {
    Flow[ValidationStreamData]
      .map(mapToJson)
      .map(validateWithSchema)
      .map(convertErrors)
  }

  private def validateWithSchema: JsonData => ValidationErrors = (data: JsonData) => {
    val errors = JsonSchemaValidators.validateJson(BASE_SCHEMA, data.jsonData)
    ValidationErrors(BASE_SCHEMA_VALIDATION, data.identifier, errors)
  }

  private def convertErrors: ValidationErrors => (String, Set[Error]) = (jsonErrors: ValidationErrors) => {
    jsonErrors.identifier -> jsonErrors.errors.map(error => Error(error.getInstanceLocation.getName(0), error.getMessage))
  }

  private def mapToJson = (data: ValidationStreamData) => {
    val json = """
    {
        "description" : "hello",
        "date_last_modified" : "12/10/2013"
    }""".stripMargin
    JsonData(data.schemaDefinition, data.metadata.identifier, json)
  }

  sealed abstract class JsonValidationErrorReason(val reason: String)

  object JsonValidationErrorReasons {
    final case object BASE_SCHEMA_VALIDATION extends JsonValidationErrorReason("/schema/baseSchema.schema.json")
  }
}
