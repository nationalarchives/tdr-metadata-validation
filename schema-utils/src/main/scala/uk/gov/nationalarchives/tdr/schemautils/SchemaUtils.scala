package uk.gov.nationalarchives.tdr.schemautils

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}

import java.util
import scala.collection.mutable
import scala.jdk.CollectionConverters._

object SchemaUtils {
  private val BASE_SCHEMA = "/metadata-schema/baseSchema.schema.json"

  private lazy val baseSchemaNode: JsonNode = new ObjectMapper().readTree(getClass.getResourceAsStream(BASE_SCHEMA))

  /** Retrieves a JSON schema's properties
    *
    * @param schemaNode
    *   The JSON node of the schema. Defaults to the Base Schema Node
    * @return
    *   Set of all the schema's properties
    */
  def schemaProperties(schemaNode: JsonNode = baseSchemaNode): mutable.Set[util.Map.Entry[String, JsonNode]] = {
    schemaNode.get("properties").properties().asScala
  }

  /** Retrieves a Base Schema property
    *
    * @param reference
    *   The '$ref' of the Base Schema node. For example: classpath:/metadata-schema/baseSchema.schema.json#/properties/file_path
    * @return
    *   The JSON node of the referenced property
    */
  def baseSchemaNode(reference: String): Option[(String, JsonNode)] = {
    val basePropertyName = reference.split("/").last
    val baseSchemaProperties = schemaProperties()
    baseSchemaProperties.filter(p => p.getKey == basePropertyName) match {
      case res if res.size == 1 => Some(res.head.getKey -> res.head.getValue)
      case _                    => None
    }
  }

  /** Retrieves the key to validate property against
    *
    * @param alternateKeyName
    *   The alternate key name that needs to be validated
    * @param alternateKeyValue
    *   The alternate key value that needs to be validated
    * @return
    *   The key for the property that can be used for validation
    * @example
    *
    * Given the following property defined in the Base Schema:
    *
    * { "base_schema_property": { "alternateKeys": [ { "alternateHeader": "AlternateName" } ] } }
    *
    * calling the function with 'alternateKeyName' = 'alternateHeader' and 'alternateKeyValue' = 'AlternateName' would return 'base_schema_property'
    */
  def convertToValidationKey(alternateKeyName: String, alternateKeyValue: String): String = {
    baseSchemaNode
      .at("/properties")
      .properties()
      .asScala
      .find(property => property.getValue.at("/alternateKeys").asScala.exists(p => p.at(s"/$alternateKeyName").asText() == alternateKeyValue))
      .map(_.getKey)
      .getOrElse("")
  }

  /** Converts a property key from source schema to alternate key
    *
    * @param alternateKeyName
    *   Alternate key name to convert the property to
    * @param propertyKey
    *   Property key to convert
    * @param sourceSchemaNode
    *   JSON node of the schema to use for conversion. Defaults to Base Schema
    * @return
    *   Converted property name
    * @example
    *   * Given the following property defined in the Base Schema: * * { * "base_schema_property": { * "alternateKeys": [ * { * "alternateHeader": "AlternateName" * } * ] * } * } *
    *   * calling the function with 'alternateKeyName' = 'alternateHeader' and 'propertyKey' = 'base_schema_property' would return 'AlternateName'
    */
  def convertToAlternateKey(alternateKeyName: String, propertyKey: String, sourceSchemaNode: JsonNode = baseSchemaNode): String = {
    sourceSchemaNode.at(s"/properties/$propertyKey/alternateKeys/0/$alternateKeyName").asText()
  }
}
