package uk.gov.nationalarchives.tdr.validation

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.scaladsl.{Sink, Source}
import org.apache.pekko.testkit.{ImplicitSender, TestKit}
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.{AnyWordSpec, AnyWordSpecLike}
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaDefinition.BASE_SCHEMA
import uk.gov.nationalarchives.tdr.validation.schema.MetadataValidationJsonSchema.{ObjectMetadata, ValidationStreamData, validationFlow}
import uk.gov.nationalarchives.tdr.validation.schema.MetadataValidationJsonSchema

import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.concurrent.duration.DurationInt

class MetadataValidationJsonSchemaSpec extends TestKit(ActorSystem("MySpec")) with ImplicitSender with AnyWordSpecLike {

  "CsvToJsonUtils" should {

    "validate uuid in correct format" in {
      val dlm = Metadata("Date last modified", "23/12/2023")
      val description = Metadata("Description", "Hello")
      val language = Metadata("Language", "Banana")
      val data: Set[ObjectMetadata] = Set(
        ObjectMetadata("file1", Set(dlm, description, language)),
        ObjectMetadata("file2", Set(dlm, description))
      )
      println("Hello")
      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data)
      println(validationErrors)

    }
  }

  val json = """
    {
        "description" : "hello",
        "end_date" : "2044-06-30T01:20+02:00",
        "language" : ["dd"]
    }""".stripMargin
}
