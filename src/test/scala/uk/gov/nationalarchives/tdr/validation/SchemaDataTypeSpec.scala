package uk.gov.nationalarchives.tdr.validation

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.networknt.schema._
import org.apache.pekko.Done
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.stream.connectors.csv.scaladsl.{CsvParsing, CsvToMap}
import org.apache.pekko.stream.scaladsl.{FileIO, Sink}
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import ujson.Value
import uk.gov.nationalarchives.tdr.validation.schema.{FileExistsValidator, InThePastValidator}

import java.io.InputStream
import java.nio.file.Paths
import java.util
import scala.collection.IterableOnce.iterableOnceExtensionMethods
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Success, Try}

class SchemaDataTypeSpec extends AnyWordSpec {

  "JSON schema validation" should {

    val schemaPath = "/schema/closureSchema.schema.json"
    val dataPath = "/data/testData.json"
    val schemaInputStream = getClass.getResourceAsStream(schemaPath)
    val schema = getJsonSchemaFromStreamContentV7(schemaInputStream)
    val dataInputStream = getClass.getResourceAsStream(dataPath)
    val node = getJsonNodeFromStreamContent(dataInputStream)

    /*
     * Data driven map
     */
    val headerMapper: Map[String, String] = {
      val nodeSchema = getJsonNodeFromStreamContent(getClass.getResourceAsStream("/schema/baseSchema.schema.json"))
      val r = ujson.read(nodeSchema.toPrettyString)
      val properties: Value = r("properties")
      properties.obj.foldLeft(Map.empty[String, String])((map, x) => {
        val tdrName = Try({ x._2("tdrName").str })
        val acc = tdrName match {
          case Success(y) => map + (y -> x._1)
          case _          => map
        }
        val tdrDescription = Try({ x._2("tdrDescription").str })
        tdrDescription match {
          case Success(y) => acc + (y -> x._1)
          case _          => acc
        }
      })
    }

    println(headerMapper)

    import scala.jdk.CollectionConverters._
    import play.api.libs.json._

    implicit val actorSystem: ActorSystem[Nothing] = ActorSystem[Nothing](Behaviors.empty, "pekko-connectors-samples")

    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    def mapToLineRow(input: Map[String, String]) = {
      val a: Map[String, Any] = input.map({ case (key, value) => (headerMapper.getOrElse(key, key), transformValue(value)) })
      val p = mapper.writeValueAsString(a)
      println(p)
      val r = schema.validate(p, InputFormat.JSON)
      r

    }

    "validate uuid in correct format" in {
      val file = Paths.get("/home/ian/Downloads/Metadata_Basic.csv")
      val future =
        FileIO
          .fromPath(file)
          .via(CsvParsing.lineScanner())
          .via(CsvToMap.toMapAsStrings())
          .async
          .map(mapToLineRow)
          .runWith(Sink.foreach(println))

      Await.result(future, Duration("20 seconds"))
      // val errors = schema.validate(node)
      assert(3 === 3)

    }

  }

  def getJsonSchemaFromStreamContentV7(schemaContent: InputStream): JsonSchema = {
    val IRI = SchemaId.V7

    val sch = JsonMetaSchema.getV7
    sch.getKeywords.put("inThePast", new InThePastValidator)
    sch.getKeywords.put("fileExists", new FileExistsValidator)
    val factory1 = new JsonSchemaFactory.Builder()
      .defaultMetaSchemaIri(IRI)
      .metaSchema(sch)
      .build()

    val config = new SchemaValidatorsConfig()
    config.setFormatAssertionsEnabled(true)

    factory1.getSchema(schemaContent, config)
  }

  def transformValue(value: String): Any = {

    val valueMap = Map("Closed" -> "closed_for", "Open" -> "open_on_transfer", "No" -> "FALSE", "Yes" -> "true")
    def convertToNull(value: String) = {
      value match {
        case "" => null
        case _ =>
          if (value.forall(Character.isDigit)) value.toInt
          else value
      }
    }
    valueMap.getOrElse(value, convertToNull(value))
  }

  protected def getJsonNodeFromStreamContent(content: InputStream): JsonNode = {
    val mapper = new ObjectMapper()
    mapper.readTree(content)
  }
}
