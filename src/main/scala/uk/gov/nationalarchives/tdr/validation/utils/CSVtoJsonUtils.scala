package uk.gov.nationalarchives.tdr.validation.utils

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import ujson.Obj
import ujson.Value.Value
import uk.gov.nationalarchives.tdr.schemautils.ConfigUtils
import uk.gov.nationalarchives.tdr.schemautils.ConfigUtils.{ARRAY_SPLIT_CHAR, MetadataConfiguration}
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaDefinition.BASE_SCHEMA

import java.io.InputStream
import scala.util.Try

class CSVtoJsonUtils {

  val nodeSchema: JsonNode = getJsonNodeFromStreamContent(getClass.getResourceAsStream(BASE_SCHEMA.schemaLocation))
  private val json: Value = ujson.read(nodeSchema.toPrettyString)
  private lazy val metadataConfiguration: MetadataConfiguration = ConfigUtils.loadConfiguration

  private lazy val propertyValueConverterMap: Map[String, ConvertedProperty] = (for {
    (propertyName, propertyValue) <- json("properties").obj
    propertyTypes = getPropertyType(propertyValue.obj)
    headerName = metadataConfiguration.propertyToOutputMapper("tdrFileHeader")(propertyName)
    headerMappings = Seq(headerName -> ConvertedProperty(propertyName, convertValueFunction(propertyType = propertyTypes)))
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

  private def getJsonNodeFromStreamContent(content: InputStream): JsonNode = {
    val mapper = new ObjectMapper()
    mapper.readTree(content)
  }

  private def getPropertyType(propertyValue: ujson.Obj): String = {
    propertyValue.obj.get("type") match {
      case Some(ujson.Str("array")) =>
        val itemsType: Option[String] = getItemsType(propertyValue)
        s"array${itemsType.map("_" + _).getOrElse("")}"
      case Some(ujson.Str(singleType))              => singleType
      case Some(ujson.Arr(types)) if types.nonEmpty =>
        val filteredTypes = types.filterNot(_.str == "null")
        val itemsType = getItemsType(propertyValue)
        s"${filteredTypes.headOption.map(_.str).getOrElse("")}${itemsType.map("_" + _).getOrElse("")}"
      case _ => "unknown"
    }
  }

  private def getItemsType(propertyValue: Obj) = {
    val itemsType = propertyValue.obj.get("items").collect { case obj: Obj =>
      getPropertyType(obj)
    }
    itemsType
  }

  private def convertValueFunction(propertyType: String): String => Any = {
    propertyType match {
      case "integer"       => (str: String) => Try(str.toInt).getOrElse(str)
      case "array_string"  => (str: String) => if (str.isEmpty) null else str.split(ARRAY_SPLIT_CHAR)
      case "array_integer" => (str: String) => if (str.isEmpty) null else str.split(ARRAY_SPLIT_CHAR).map(s => Try(s.toInt).getOrElse(s))
      case "boolean"       =>
        (str: String) =>
          str.toUpperCase match {
            case "YES" | "TRUE" => true
            case "NO" | "FALSE" => false
            case _              => str
          }
      case _ => (str: String) => if (str.isEmpty) null else str
    }
  }

  private case class ConvertedProperty(propertyName: String, convertValueFunc: String => Any)
}
