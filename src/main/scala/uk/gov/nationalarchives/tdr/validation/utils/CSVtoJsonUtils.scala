package uk.gov.nationalarchives.tdr.validation.utils

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import java.io.InputStream

class CSVtoJsonUtils {

  private val schemaPath = "/schema/baseSchema.schema.json"
//  val dataPath = "/data/testData.json"
//  val dataInputStream = getClass.getResourceAsStream(dataPath)
//  val node = getJsonNodeFromStreamContent(dataInputStream)

  private val nodeSchema = getJsonNodeFromStreamContent(getClass.getResourceAsStream(schemaPath))
  private val json = ujson.read(nodeSchema.toPrettyString)

  // Helper function to extract type and format from the JSON value
  private def extractTypeAndFormat(propertyValue: ujson.Obj): (String, Option[String]) = {
    val propertyType = propertyValue.obj.get("type") match {
      case Some(ujson.Str(singleType)) => singleType
      case Some(ujson.Arr(types)) if types.nonEmpty => types.head.str
      case _ => "unknown"
    }

    val propertyFormat = propertyValue.obj.get("format").map(_.str)

    (propertyType, propertyFormat)
  }

  private def convertValueFunction(propertyType: String, propertyFormat: Option[String]): String => Any = {
    propertyType match {
      case "number" => (str: String) => str.toInt // error checks can be added
      case "array" => (str: String) => str.split("\\|")
      case "boolean" => {
        case "Yes" => true
        case "No" => false
        case str => str
      }
      case _ => (str: String) => str
    }
  }

  private val propertyValueConverterMap: Map[String, (String, String => Any)] = (for {
    (propertyName, propertyValue) <- json("properties").obj
    propertyTypes = extractTypeAndFormat(propertyValue.obj)
    //Below case for when alternateKeys is not present to use the propertyName instead
    headerMappings = propertyValue.obj.get("alternateKeys") match {
      case Some(alternateKeys) =>
        for {
          alternateKey <- alternateKeys.arr
          header <- alternateKey.obj.get("tdrFileHeader").toSeq
        } yield header.str -> (propertyName, convertValueFunction(propertyType = propertyTypes._1, propertyFormat = propertyTypes._2))
      case None =>
        Seq(propertyName -> (propertyName, convertValueFunction(propertyType = propertyTypes._1, propertyFormat = propertyTypes._2)))
    }
    headerMapping <- headerMappings
  } yield headerMapping).toMap


  def mapToLineRow(input: Map[String, String]): String = {
    val mapper = new ObjectMapper().registerModule(DefaultScalaModule)
    val a: Map[String, Any] = input.map { case (key, value) =>
      propertyValueConverterMap.get(key) match {
        case Some(objectProperty) =>
          objectProperty._1 -> objectProperty._2(value)
        case None =>
          key -> value
      }
    }
    val generatedJson = mapper.writeValueAsString(a)
    generatedJson
  }

  protected def getJsonNodeFromStreamContent(content: InputStream): JsonNode = {
    val mapper = new ObjectMapper()
    mapper.readTree(content)
  }
}
