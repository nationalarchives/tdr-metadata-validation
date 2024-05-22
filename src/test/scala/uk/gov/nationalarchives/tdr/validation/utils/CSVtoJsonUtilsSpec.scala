package uk.gov.nationalarchives.tdr.validation.utils

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.stream.connectors.csv.scaladsl.{CsvParsing, CsvToMap}
import org.apache.pekko.stream.scaladsl.{FileIO, Sink}
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

import java.nio.file.Paths
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class CSVtoJsonUtilsSpec extends AnyWordSpec {

  "CSVtoJsonUtils" should {

    "return the correct json given a csv map[Header, value]" in {
      implicit val actorSystem: ActorSystem[Nothing] = ActorSystem[Nothing](Behaviors.empty, "pekko-connectors-samples")
      val file = Paths.get("/home/thanh/Downloads/TDR-2024-L66T-2024-05-10T11-28-47.csv")
      val utils = new CSVtoJsonUtils()
      val future =
        FileIO
          .fromPath(file)
          .via(CsvParsing.lineScanner())
          .via(CsvToMap.toMapAsStrings())
          .async
          .map(utils.convertToJSONString)
          .runWith(Sink.foreach(println))

      Await.result(future, Duration("20 seconds"))
      assert(Set.empty.size === 0)
    }

    "Return the correct JSON for a `number` type" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("Closure Period" -> "5")
      val result = utils.convertToJSONString(testData)
      assert(result == """{"closure_period":5}""")
    }

    "Return the correct JSON for a `array` type" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("FOI exemption code" -> "37(1)(ab)|44")
      val result = utils.convertToJSONString(testData)
      assert(result == """{"foi_exemption_code":["37(1)(ab)","44"]}""")
    }

    "Return the correct JSON for a `boolean` type" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("Is the title sensitive for the public?" -> "Yes", "Is the description sensitive for the public?" -> "No")
      val result = utils.convertToJSONString(testData)
      assert(result == """{"title_closed":true,"description_closed":false}""")
    }

    "Preserve key-value pairs when key is not in schema" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("unknown" -> "some value")
      val result = utils.convertToJSONString(testData)
      assert(result == """{"unknown":"some value"}""")
    }
  }
}
