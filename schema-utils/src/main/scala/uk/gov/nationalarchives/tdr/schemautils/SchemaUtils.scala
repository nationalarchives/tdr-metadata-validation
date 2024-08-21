package uk.gov.nationalarchives.tdr.schemautils

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}

import java.io.InputStream

object SchemaUtils {
  val baseSchema: JsonNode = schema("/metadata-schema/baseSchema.schema.json")

  private def getJsonNodeFromStreamContent(content: InputStream): JsonNode = {
    val mapper = new ObjectMapper()
    mapper.readTree(content)
  }

  // Return schema as JsonNode for clients to manipulate as required
  def schema(schemaLocation: String): JsonNode = {
    getJsonNodeFromStreamContent(getClass.getResourceAsStream(schemaLocation))
  }

  def convertToValidationKey(alternateKey: String, propertyKey: String): String = ???

  def convertToAlternateKey(alternateKey: String, propertyKey: String): String = ???
}
