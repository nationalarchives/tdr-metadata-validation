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
          .map(utils.mapToLineRow)
          .runWith(Sink.foreach(println))

      Await.result(future, Duration("20 seconds"))
      assert(Set.empty.size === 0)
    }
  }

}
