package uk.gov.nationalarchives.tdr.schemautils

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}

import scala.jdk.CollectionConverters.IterableHasAsScala

object SchemaUtils {
  val BASE_SCHEMA = "/metadata-schema/baseSchema.schema.json"

  private lazy val schemaNode: JsonNode = new ObjectMapper().readTree(getClass.getResourceAsStream(BASE_SCHEMA))
  private lazy val schemaProperties = schemaNode
    .at("/properties")
    .properties()
    .asScala

  /** Provides a property mapping between an original property name and an alternate property name using the base schema:
    * https://github.com/nationalarchives/da-metadata-schema/blob/main/metadata-schema/baseSchema.schema.json
    *
    * @param originalKey
    *   The original key that needs to be mapped to the alternate key
    * @param alternateKey
    *   The alternate key that for the original key.
    * @return
    *   Map with the 'originalKey' as the key and the 'alternateKey' as the value. If no value found for the 'originalKey' or 'alternateKey' then defaults to the property name
    *   defined in the base schema
    */

  def originalKeyToAlternateKeyMapping(originalKey: String, alternateKey: String): Map[String, String] = {
    val altKeys = schemaProperties
      .map(p => p.getKey -> p.getValue.at("/alternateKeys").asScala)
      .toMap

    altKeys
      .map(altKey => {
        val defaultKeyValue = altKey._1
        altKey._2.map(jn => {
          val original = jn.at(s"/$originalKey")
          val required = jn.at(s"/$alternateKey")
          original.asText(defaultKeyValue) -> required.asText(defaultKeyValue)
        })
      })
      .flatten
      .toMap
  }

  def convertToValidationKey(alternateKeyName: String, alternateKeyValue: String): String = {
    schemaProperties
      .find(property => property.getValue.at("/alternateKeys").asScala.exists(p => p.at(s"/$alternateKeyName").asText() == alternateKeyValue))
      .map(_.getKey)
      .getOrElse("")
  }

  def convertToAlternateKey(alternateKeyName: String, propertyKey: String): String = {
    schemaNode.at(s"/properties/$propertyKey/alternateKeys/0/$alternateKeyName").asText()
  }
}
