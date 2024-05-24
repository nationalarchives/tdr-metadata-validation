package uk.gov.nationalarchives.tdr.validation

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.scaladsl.{Sink, Source}
import org.apache.pekko.testkit.{ImplicitSender, TestKit}
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.{AnyWordSpec, AnyWordSpecLike}
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaDefinition.BASE_SCHEMA
import uk.gov.nationalarchives.tdr.validation.schema.MetadataValidationJsonSchema.{ObjectMetadata, ValidationStreamData, validationFlow}
import uk.gov.nationalarchives.tdr.validation.schema.{CsvToJsonUtils, MetadataValidationJsonSchema}

import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.concurrent.duration.DurationInt

class MetadataValidationJsonSchemaSpec extends TestKit(ActorSystem("MySpec")) with ImplicitSender with AnyWordSpecLike {

  "CsvToJsonUtils" should {

    "validate uuid in correct format" in {
      val dlm = Metadata("DateLastModified", "23/12/2023")
      val description = Metadata("Description", "Hello")
      val data: Set[ObjectMetadata] = Set(
        ObjectMetadata("file1", Set(dlm, description)),
        ObjectMetadata("file2", Set(dlm, description))
      )

      println(MetadataValidationJsonSchema.validate(BASE_SCHEMA, data))
      val streamData = data.map(metadata => ValidationStreamData(BASE_SCHEMA, metadata))
      val execution = Source(streamData).via(validationFlow).runWith(Sink.foreach(println))
      val result = Await.result(execution, 10.seconds)
      println(result)
    }
  }
}
