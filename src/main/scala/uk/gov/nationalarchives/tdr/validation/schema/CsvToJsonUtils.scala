package uk.gov.nationalarchives.tdr.validation.schema

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.networknt.schema._
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.stream.connectors.csv.scaladsl.{CsvParsing, CsvToMap}
import org.apache.pekko.stream.scaladsl.FileIO
import ujson.Value

import java.io.InputStream
import java.nio.file.Paths
import java.text.SimpleDateFormat
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Success, Try}

class CsvToJsonUtils {

  implicit val actorSystem: ActorSystem[Nothing] = ActorSystem[Nothing](Behaviors.empty, "pekko-connectors-samples")
  // This will be grabbed from S3
  val schemaPath: String = "/schema/baseSchema.schema.json"
  val schemaInputStream: InputStream = getClass.getResourceAsStream(schemaPath)
  val schema: JsonSchema = getJsonSchemaFromStreamContentV7(schemaInputStream)

  /*
   * Data driven map
   */
  val headerMapper: Map[String, String] = {
    val nodeSchema = getJsonNodeFromStreamContent(getClass.getResourceAsStream(schemaPath))
    val json = ujson.read(nodeSchema.toPrettyString)
    val properties: Value = json("properties")
    properties.obj.foldLeft(Map.empty[String, String])((accMap, values) => {
      val (propertyName, properties) = values
      val tdrName = Try({
        properties("alternateKeys")
      })
      tdrName match {
        case Success(value) => accMap + (value(0)("tdrFileHeader").str -> propertyName)
        case _              => accMap
      }
    })
  }

  def generateJsonFromSchema() = {
    val file = Paths.get("/home/thanh/Downloads/TDR-2024-L66T-2024-05-10T11-28-47.csv")
    val future =
      FileIO
        .fromPath(file)
        .via(CsvParsing.lineScanner())
        .via(CsvToMap.toMapAsStrings())
        .async
        .map(mapToLineRow)
        .runForeach(println(_))

    Await.result(future, Duration("20 seconds"))
  }

  private def mapToLineRow(input: Map[String, String]) = {
    val generatedJsonAsString = mapRowToJson(input)
    schema.validate(generatedJsonAsString, InputFormat.JSON)
  }

  private def mapRowToJson(input: Map[String, String]) = {
    val mapper: ObjectMapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    val generatedJson: Map[String, Any] = input.map({ case (key, value) => (headerMapper.getOrElse(key, key), transformValue(headerMapper.getOrElse(key, key), value)) })
    mapper.writeValueAsString(generatedJson)

  }

  private def transformValue(key: String, value: String): Any = {
    key match {
      case "FOI exemption code" | "language" => value.split("\\|")
      case "closure_period" =>
        value.length match {
          case 0 => 0
          case _ => value.toInt
        }
      case "date_last_modified" =>
        value.length match {
          case 0 => 0
          case _ =>
            val sourceFormat = new SimpleDateFormat("dd/MM/yyyy")
            Try { sourceFormat.parse(value) } match {
              case Success(a) => a
              case _          => value
            }
        }
      case "file_size" =>
        value.length match {
          case 0 => 0
          case _ => value.toInt
        }
      case "title_closed" | "description_closed" =>
        value match {
          case "Yes" => true
          case "No"  => false
          case _     => value
        }
      case _ => value
    }
  }

  private def getJsonSchemaFromStreamContentV7(schemaContent: InputStream): JsonSchema = {
    val IRI = SchemaId.V7
    val sch = JsonMetaSchema.getV7
    val factory1 = new JsonSchemaFactory.Builder()
      .defaultMetaSchemaIri(IRI)
      .metaSchema(sch)
      .build()

    val config = new SchemaValidatorsConfig()
    config.setFormatAssertionsEnabled(true)

    factory1.getSchema(schemaContent, config)
  }

  private def getJsonNodeFromStreamContent(content: InputStream): JsonNode = {
    val mapper = new ObjectMapper()
    mapper.readTree(content)
  }
}
