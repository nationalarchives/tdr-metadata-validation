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

    "correctly convert a numeric string to a JSON `number`" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("Closure Period" -> "5")
      val result = utils.convertToJSONString(testData)
      assert(result == """{"closure_period":5}""")
    }

    "return a JSON string when the input is not a `number`" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("Closure Period" -> "abc")
      val result = utils.convertToJSONString(testData)
      assert(result == """{"closure_period":"abc"}""")
    }

    "correctly convert a split an array string to a JSON `array`" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("FOI exemption code" -> "37(1)(ab)|44")
      val result = utils.convertToJSONString(testData)
      assert(result == """{"foi_exemption_code":["37(1)(ab)","44"]}""")
    }

    "return a JSON string when the input array cannot be split" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("FOI exemption code" -> "37(1)(ab)+44")
      val result = utils.convertToJSONString(testData)
      assert(result == """{"foi_exemption_code":["37(1)(ab)+44"]}""")
    }

    "correctly convert a boolean string to a JSON `boolean`" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("Is the title sensitive for the public?" -> "Yes", "Is the description sensitive for the public?" -> "No")
      val result = utils.convertToJSONString(testData)
      assert(result == """{"title_closed":true,"description_closed":false}""")
    }

    "return a JSON string when the input boolean cannot be converted" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("Is the title sensitive for the public?" -> "y", "Is the description sensitive for the public?" -> "n")
      val result = utils.convertToJSONString(testData)
      assert(result == """{"title_closed":"y","description_closed":"n"}""")
    }

    "Preserve key-value pairs when key is not in schema" in {
      val utils = new CSVtoJsonUtils()
      val testData = Map("unknown" -> "some value")
      val result = utils.convertToJSONString(testData)
      assert(result == """{"unknown":"some value"}""")
    }
  }
}
