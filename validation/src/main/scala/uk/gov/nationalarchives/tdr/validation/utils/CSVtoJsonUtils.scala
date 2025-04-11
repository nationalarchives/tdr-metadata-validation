package uk.gov.nationalarchives.tdr.validation.utils

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import ujson.Value.Value
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaDefinition.BASE_SCHEMA

import java.io.InputStream
import scala.util.Try

class CSVtoJsonUtils {

  private case class ConvertedProperty(propertyName: String, convertValueFunc: String => Any)
  val nodeSchema: JsonNode = getJsonNodeFromStreamContent(getClass.getResourceAsStream(BASE_SCHEMA.schemaLocation))
  private val json: Value = ujson.read(nodeSchema.toPrettyString)

  private def getJsonNodeFromStreamContent(content: InputStream): JsonNode = {
    val mapper = new ObjectMapper()
    mapper.readTree(content)
  }

  // Extracts type from JSON value
  private def getPropertyType(propertyValue: ujson.Obj): String = {
    propertyValue.obj.get("type") match {
      case Some(ujson.Str(singleType))              => singleType
      case Some(ujson.Arr(types)) if types.nonEmpty => types.head.str
      case _                                        => "unknown"
    }
  }

  private def convertValueFunction(propertyType: String): String => Any = {
    propertyType match {
      case "integer" => (str: String) => Try(str.toInt).getOrElse(str)
      case "array"   => (str: String) => if (str.isEmpty) "" else str.split("\\|")
      case "boolean" =>
        (str: String) =>
          str.toUpperCase match {
            case "YES" => true
            case "NO"  => false
            case _     => str
          }
      case _ => (str: String) => str
    }
  }

  private val propertyValueConverterMap: Map[String, ConvertedProperty] = (for {
    (propertyName, propertyValue) <- json("properties").obj
    propertyTypes = getPropertyType(propertyValue.obj)
    // Use propertyName if alternateKeys is absent
    headerMappings = propertyValue.obj.get("alternateKeys") match {
      case Some(alternateKeys) =>
        for {
          alternateKey <- alternateKeys.arr
          header <- alternateKey.obj.get("tdrFileHeader").toSeq
        } yield header.str -> ConvertedProperty(propertyName, convertValueFunction(propertyType = propertyTypes))
      case None =>
        Seq(propertyName -> ConvertedProperty(propertyName, convertValueFunction(propertyType = propertyTypes)))
    }
    headerMapping <- headerMappings
  } yield headerMapping).toMap

  // Converts a CSV key-value pair to a JSON string with correct types
  def convertToJSONString(input: Map[String, String]): String = {
    val mapper = new ObjectMapper().registerModule(DefaultScalaModule)
    val dataConvertedToSchemaDefinitions: Map[String, Any] = input.map { case (key, value) =>
      propertyValueConverterMap.get(key) match {
        case Some(convertedProperty: ConvertedProperty) =>
          convertedProperty.propertyName -> convertedProperty.convertValueFunc(value)
        case None =>
          key -> value
      }
    }
    val generatedJson = mapper.writeValueAsString(dataConvertedToSchemaDefinitions)
    generatedJson
  }
}
