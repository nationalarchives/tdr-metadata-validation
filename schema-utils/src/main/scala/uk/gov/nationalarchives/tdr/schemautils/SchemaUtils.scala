package uk.gov.nationalarchives.tdr.schemautils

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}

import scala.jdk.CollectionConverters.IterableHasAsScala

object SchemaUtils {
  private val BASE_SCHEMA = "/metadata-schema/baseSchema.schema.json"

  private lazy val schemaNode: JsonNode = new ObjectMapper().readTree(getClass.getResourceAsStream(BASE_SCHEMA))

  def convertToValidationKey(alternateKeyName: String, alternateKeyValue: String): String = {
    schemaNode
      .at("/properties")
      .properties()
      .asScala
      .find(property => property.getValue.at("/alternateKeys").asScala.exists(p => p.at(s"/$alternateKeyName").asText() == alternateKeyValue))
      .map(_.getKey)
      .getOrElse("")
  }

  def convertToAlternateKey(alternateKeyName: String, propertyKey: String): String = {
    schemaNode.at(s"/properties/$propertyKey/alternateKeys/0/$alternateKeyName").asText()
  }

  def getMetadataProperties(propertyType: String): List[String] = {
    schemaNode
      .at("/properties")
      .properties()
      .asScala
      .filter(_.getValue.at("/propertyType").asText() == propertyType)
      .map(_.getKey)
      .toList
  }

  def getPropertyField(propertyKey: String, fieldName: String): JsonNode = {
    schemaNode.at(s"/properties/$propertyKey/$fieldName")
  }

}
