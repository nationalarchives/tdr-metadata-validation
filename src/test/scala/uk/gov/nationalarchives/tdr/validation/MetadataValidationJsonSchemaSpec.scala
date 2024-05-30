package uk.gov.nationalarchives.tdr.validation

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.scaladsl.{Sink, Source}
import org.apache.pekko.testkit.{ImplicitSender, TestKit}
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.{AnyWordSpec, AnyWordSpecLike}
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaDefinition.BASE_SCHEMA
import uk.gov.nationalarchives.tdr.validation.schema.MetadataValidationJsonSchema.{ObjectMetadata, ValidationStreamData, validationFlow}
import uk.gov.nationalarchives.tdr.validation.schema.MetadataValidationJsonSchema

class MetadataValidationJsonSchemaSpec extends TestKit(ActorSystem("MySpec")) with ImplicitSender with AnyWordSpecLike {

  "MetadataValidationJsonSchema" should {

    "stub for tests" in {
      val dlm = Metadata("Date last modified", "2023-12-05")
      val description = Metadata("Description", "Hello")
      val language = Metadata("Language", "Banana")
      val data: Set[ObjectMetadata] = Set(
        ObjectMetadata("file1", Set(dlm, description, language)),
        ObjectMetadata("file2", Set(dlm, description))
      )

      val validationErrors = MetadataValidationJsonSchema.validate(BASE_SCHEMA, data)
      println(validationErrors)

    }
  }
}
