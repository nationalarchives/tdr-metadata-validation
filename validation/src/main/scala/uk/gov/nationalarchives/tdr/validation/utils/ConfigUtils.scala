package uk.gov.nationalarchives.tdr.validation.utils

import cats.data.Reader
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import io.circe.generic.auto._
import io.circe.jawn.decode
import ujson.Value.Value

import scala.io.Source
import scala.util.Using

object ConfigUtils {

  val configParameters: ConfigParameters = ConfigParameters(loadBaseSchema, loadConfigFile)

  def loadConfiguration(configurationParameters: ConfigParameters): MetadataConfiguration = {
    val csvConfigurationReader = for {
      altHeaderToPropertyMapper <- Reader(inputToPropertyMapper)
      propertyToAltHeaderMapper <- Reader(propertyToOutputMapper)
      downloadProperties <- Reader(getRequiredColumns)
    } yield MetadataConfiguration(altHeaderToPropertyMapper, propertyToAltHeaderMapper, downloadProperties)
    csvConfigurationReader.run(configurationParameters)
  }

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

  private def getRequiredColumns(configurationParameters: ConfigParameters): String => List[(String, Int)] = {
    val configItems: Map[String, List[(String, Int)]] = configurationParameters.baseConfig
      .getOrElse(Config(List.empty[ConfigItem]))
      .configItems
      .flatMap(item => item.downloadFilesOutputs.map(output => (output.domain, (item.key, output.columnIndex))))
      .groupBy(_._1)
      .view
      .mapValues(_.map(_._2))
      .toMap

    domain => configItems.getOrElse(domain, List.empty)
  }

  def loadBaseSchema: Value = {
    val utils = new CSVtoJsonUtils()
    val nodeSchema: JsonNode = utils.nodeSchema
    ujson.read(nodeSchema.toPrettyString)
  }

  def loadConfigFile: Either[io.circe.Error, Config] = {
    val nodeSchema = Using(Source.fromResource("config.json"))(_.mkString)
    val mapper = new ObjectMapper()
    val data = mapper.readTree(nodeSchema.get).toPrettyString
    decode[Config](data)
  }

  case class MetadataConfiguration(
      inputToPropertyMapper: (String, String) => String,
      propertyToOutputMapper: (String, String) => String,
      downloadProperties: String => List[(String, Int)]
  )

  case class ConfigParameters(baseSchema: Value, baseConfig: Either[io.circe.Error, Config])

  case class DownloadFilesOutput(domain: String, columnIndex: Int)

  case class ConfigItem(key: String, downloadFilesOutputs: List[DownloadFilesOutput])

  case class Config(configItems: List[ConfigItem])

  case class MapperConfiguration(property: String, alternateHeaders: Map[String, String])
}
