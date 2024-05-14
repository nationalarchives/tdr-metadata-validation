package uk.gov.nationalarchives.tdr.validation

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.nationalarchives.tdr.validation.schema.CsvToJsonUtils

class SchemaDataTypeSpec extends AnyWordSpec {

  implicit val actorSystem: ActorSystem[Nothing] = ActorSystem[Nothing](Behaviors.empty, "pekko-connectors-samples")
  "CsvToJsonUtils" should {

    "validate uuid in correct format" in {
      val csvToJsonUtils = new CsvToJsonUtils
      val result = csvToJsonUtils.generateJsonFromSchema()
//      result.onComplete {
//        case Success(rows) =>
//          println(rows)
//          rows
//        case Failure(exception) =>
//          println(s"Error processing CSV: $exception")
//      }
    }
  }
}
