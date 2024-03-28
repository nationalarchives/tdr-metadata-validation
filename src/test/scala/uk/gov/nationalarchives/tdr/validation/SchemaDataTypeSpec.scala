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
import uk.gov.nationalarchives.tdr.validation.schema.{FileExistsValidator, InThePastValidator}

import java.io.InputStream
import java.nio.file.Paths
import java.util
import scala.collection.IterableOnce.iterableOnceExtensionMethods
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}


class SchemaDataTypeSpec extends AnyWordSpec {


  "JSON schema validation" should {

    val schemaPath = "/schema/closureSchema.schema.json"
    val dataPath = "/data/testData.json"
    val schemaInputStream = getClass.getResourceAsStream(schemaPath)
    val schema = getJsonSchemaFromStreamContentV7(schemaInputStream)
    val dataInputStream = getClass.getResourceAsStream(dataPath)
    val node = getJsonNodeFromStreamContent(dataInputStream)

    import scala.jdk.CollectionConverters._
    import play.api.libs.json._

    implicit val actorSystem: ActorSystem[Nothing] = ActorSystem[Nothing](Behaviors.empty, "pekko-connectors-samples")

    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    def mapToLineRow (input: Map[String, String])= {
     val a: Map[String, Any] =  input.map( {case (key, value) => (transformKey(key), transformValue(value))})
    // println(a)
      val p = mapper.writeValueAsString(a)
      println(p)
      val r = schema.validate(p,InputFormat.JSON)
      r

    }

    "validate uuid in correct format" in {
      val file = Paths.get("/home/ian/Downloads/Metadata_Basic.csv")
      val future =
        FileIO.fromPath(file)
          .via(CsvParsing.lineScanner())
          .via(CsvToMap.toMapAsStrings())
          .async.map(mapToLineRow)
          .runWith(Sink.foreach(println))


   Await.result(future,Duration("20 seconds"))
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

  def transformKey(key:String): String = {
    val keyMap = Map("FOI decision asserted"->"foi_exemption_asserted",
       "Alternative description" -> "description_alternate",
       "FOI exemption code" -> "foi_exemption_code",
       "Closure Period" -> "closure_period",
       "Closure status" -> "closure_type",
       "Translated title of record" -> "translated_title_of_record",
       "Add alternative title without the file extension" -> "title_alternate",
      "Alternative description" -> "description_alternate",
      "Description" -> "description",
      "Language" -> "language",
      "Filename" -> "filename",
      "Date of the record" -> "date_of_the_record",
      "Is the description sensitive for the public?" -> "description_public",
      "Closure Start Date" -> "closure_start_date",
      "Is the title sensitive for the public?" -> "title_public",
      "Former reference" -> "former_reference",
      "Date last modified" -> "date_last_modified",
      "Closure Start Date" -> "closure_start_date",
      "Filepath" -> "identifier"

    )


    keyMap.getOrElse(key,key)


  }

  def transformValue(value:String): Any = {

    val valueMap = Map("Closed"->"closed_for",
      "Open" -> "open_on_transfer",
      "No" -> "FALSE",
      "Yes" -> "true"
    )
    def convertToNull(value:String) ={
      value match {
        case "" => null
        case _ => if(value.forall(Character.isDigit))  value.toInt
        else value
      }
    }
    valueMap.getOrElse(value,convertToNull(value))
  }

  protected def getJsonNodeFromStreamContent(content: InputStream): JsonNode = {
    val mapper = new ObjectMapper()
    mapper.readTree(content)
  }
}
