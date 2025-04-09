package uk.gov.nationalarchives.tdr.validation.utils

import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaDefinition.BASE_SCHEMA

import java.net.URI
import scala.io.Source
import scala.util.Using

object ConfigUtils {

  case class MapperConfigurationParameters(alternates: MapperConfiguration, inputDomain: Option[String], outputDomain: Option[String])

  case class MapperConfiguration(property: String, alternateHeaders: Map[String, String])

  def inputToPropertyMapper(configurationParameters: MapperConfigurationParameters): String => String = {
    val data = Using(Source.fromResource(BASE_SCHEMA.schemaLocation))(_.mkString)
    println(data)

    // This is a placeholder for the actual mapping logic
    val inputToPropertyMap = Map(
      "Filepath" -> "file_path"
    )
    (x: String) => inputToPropertyMap.getOrElse(x, x)
  }

  def propertyToOutputMapper(configurationParameters: MapperConfigurationParameters): String => String = {
    // This is a placeholder for the actual mapping logic
    val propertyToOutputMap = Map(
      "file_path" -> "Filepath"
    )
    (x: String) => propertyToOutputMap.getOrElse(x, x)
  }

  def inputToOutputMapper(configurationParameters: MapperConfigurationParameters): String => String = {
    // This is a placeholder for the actual mapping logic
    val inputToOutputMap = Map(
      "file_path" -> "Filepath"
    )
    (x: String) => inputToOutputMap.getOrElse(x, x)
  }

}
