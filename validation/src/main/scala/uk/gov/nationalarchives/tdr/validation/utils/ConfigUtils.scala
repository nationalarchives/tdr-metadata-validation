package uk.gov.nationalarchives.tdr.validation.utils

import cats.data.Reader
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import io.circe.generic.auto._
import io.circe.jawn.decode
import ujson.Value.Value

import scala.io.Source
import scala.util.Using

object ConfigUtils {

  private lazy val configParameters: ConfigParameters = ConfigParameters(loadBaseSchema, loadConfigFile)

  /** Loads the configuration files and returns a `MetadataConfiguration` object.
    *
    * This method uses the Reader monad to compose multiple functions that read from the configuration files. The resulting `MetadataConfiguration` object contains functions for:
    *   - Mapping alternate headers to property names.
    *   - Mapping property names to alternate headers.
    *   - Getting required columns for metadata downloads.
    *   - Getting property types.
    *
    * @return
    *   A `MetadataConfiguration` object containing the configuration mappings and functions.
    */
  def loadConfiguration: MetadataConfiguration = {
    val csvConfigurationReader = for {
      altHeaderToPropertyMapper <- Reader(inputToPropertyMapper)
      propertyToAltHeaderMapper <- Reader(propertyToOutputMapper)
      downloadProperties <- Reader(getRequiredColumns)
      propertyType <- Reader(getPropertyType)
    } yield MetadataConfiguration(altHeaderToPropertyMapper, propertyToAltHeaderMapper, downloadProperties, propertyType)
    csvConfigurationReader.run(configParameters)
  }

  /** This method takes configuration parameters and returns a function that maps a domain key to a property name. It uses the configuration file to create a mapping of alternate
    * keys to property names.
    *
    * @param configurationParameters
    *   The configuration parameters containing the base schema and configuration data.
    * @return
    *   A function that takes two parameters: the domain and the key, and returns the corresponding property name. Example: ("tdrFileHeader", "Date last modified") =>
    *   "date_last_modified".
    */
  def inputToPropertyMapper(configurationParameters: ConfigParameters): (String, String) => String = {
    val mapped = configurationParameters.baseSchema("properties").obj.foldLeft(Map.empty[String, Map[String, String]]) { case (acc, (propertyName, propertyValue)) =>
      propertyValue.obj.get("alternateKeys") match {
        case Some(alternateKeys) =>
          val updatedMap = alternateKeys.arr.foldLeft(acc) { (innerAcc, key) =>
            key.obj.foldLeft(innerAcc) { case (innerMap, (alternateKey, alternateValue)) =>
              val existingMap = innerMap.getOrElse(alternateKey, Map.empty)
              innerMap + (alternateKey -> (existingMap + (alternateValue.str -> propertyName)))
            }
          }
          updatedMap
        case None => acc
      }
    }
    (domain: String, key: String) => mapped.get(domain).flatMap(_.get(key)).getOrElse(key)
  }

  /** This method takes configuration parameters and returns a function that maps a property name to an alternate key. It uses the configuration file to create a mapping of
    * property names to alternate keys.
    *
    * @param configurationParameters
    *   The configuration parameters containing the base schema and configuration data.
    * @return
    *   A function that takes two parameters: the domain and the property name, and returns the corresponding alternate key. Example: ("tdrFileHeader", "date_last_modified") =>
    *   "Date last modified".
    */
  def propertyToOutputMapper(configurationParameters: ConfigParameters): (String, String) => String = {

    val propertyToOutputsMap = configurationParameters.baseSchema("properties").obj.foldLeft(Map.empty[String, Map[String, String]]) { case (acc, (propertyName, propertyValue)) =>
      propertyValue.obj.get("alternateKeys") match {
        case Some(alternateKeys) =>
          val updatedMap = alternateKeys.arr.foldLeft(acc) { (innerAcc, key) =>
            key.obj.foldLeft(innerAcc) { case (innerMap, (alternateKey, alternateValue)) =>
              val existingMap = innerMap.getOrElse(alternateKey, Map.empty)
              innerMap + (alternateKey -> (existingMap + (propertyName -> alternateValue.str)))
            }
          }
          updatedMap
        case None => acc
      }
    }
    (domain: String, propertyName: String) => propertyToOutputsMap.get(domain).flatMap(_.get(propertyName)).getOrElse(propertyName)
  }

  /** This method takes configuration parameters and returns a function that retrieves the required columns for a given domain metadata download. It uses the configuration file to
    * create a mapping of domains to their required columns.
    *
    * @param configurationParameters
    *   The configuration parameters containing the base schema and configuration data.
    * @return
    *   A function that takes a domain as a parameter and returns a list of tuples containing the property name and column index.
    */
  private def getRequiredColumns(configurationParameters: ConfigParameters): String => List[(String, Int)] = {
    val configItems: Map[String, List[(String, Int)]] = configurationParameters.baseConfig
      .getOrElse(Config(List.empty[ConfigItem]))
      .configItems
      .flatMap(item => item.downloadFilesOutputs.map(output => (output.domain, (item.key, output.columnIndex))))
      .groupBy(_._1)
      .view
      .mapValues(_.map(_._2))
      .toMap

    domain => configItems.getOrElse(domain, List.empty[(String, Int)]).sortBy(_._2)
  }

  /** Retrieves the property type for a given property based on the configuration parameters.
    *
    * This method creates a mapping of property names to their types using the base schema from the configuration parameters. The resulting function takes a property name as input
    * and returns the corresponding property type as a string.
    *
    * @param configParameters
    *   The configuration parameters containing the base schema.
    * @return
    *   A function that takes a property name (domain) as input and returns its type as a string.
    */
  private def getPropertyType(configParameters: ConfigParameters): String => String = {
    val propertyTypeMap = configParameters.baseSchema("properties").obj.foldLeft(Map.empty[String, String]) { case (acc, (propertyName, propertyValue)) =>
      val propertyType = propertyValue.obj.get("type") match {
        case Some(ujson.Str(singleType))              => singleType
        case Some(ujson.Arr(types)) if types.nonEmpty => types.collectFirst { case ujson.Str(value) if value != "null" => value }.getOrElse("unknown")
        case _                                        => "unknown"
      }
      acc + (propertyName -> propertyValue.obj.get("format").map(_.str).getOrElse(propertyType))

    }
    domain => propertyTypeMap.getOrElse(domain, "")
  }

  private def loadBaseSchema: Value = {
    val utils = new CSVtoJsonUtils()
    val nodeSchema: JsonNode = utils.nodeSchema
    ujson.read(nodeSchema.toPrettyString)
  }

  private def loadConfigFile: Either[io.circe.Error, Config] = {
    val nodeSchema = Using(Source.fromResource("config.json"))(_.mkString)
    val mapper = new ObjectMapper()
    val data = mapper.readTree(nodeSchema.get).toPrettyString
    decode[Config](data)
  }

  case class MetadataConfiguration(
      inputToPropertyMapper: (String, String) => String,
      propertyToOutputMapper: (String, String) => String,
      downloadProperties: String => List[(String, Int)],
      getPropertyType: String => String
  )

  case class ConfigParameters(baseSchema: Value, baseConfig: Either[io.circe.Error, Config])

  case class DownloadFilesOutput(domain: String, columnIndex: Int)

  case class ConfigItem(key: String, downloadFilesOutputs: List[DownloadFilesOutput])

  case class Config(configItems: List[ConfigItem])

}
