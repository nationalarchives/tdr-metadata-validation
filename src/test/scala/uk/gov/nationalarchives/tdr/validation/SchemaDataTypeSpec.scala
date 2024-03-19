package uk.gov.nationalarchives.tdr.validation

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.networknt.schema.{Format, Formats, JsonMetaSchema, JsonSchema, JsonSchemaFactory, NonValidationKeyword, SchemaId, SchemaValidatorsConfig, SpecVersion}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper

import java.io.InputStream
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.nationalarchives.tdr.validation.schema.GreatValidator

import java.util

class SchemaDataTypeSpec extends AnyWordSpec {

  "JSON schema validation" should {

    val schemaPath = "/schema/closureSchema.json"
    val dataPath = "/data/testData.json"
    val schemaInputStream = getClass.getResourceAsStream(schemaPath)
    val schema = getJsonSchemaFromStreamContentV7(schemaInputStream)
    val dataInputStream = getClass.getResourceAsStream(dataPath)
    val node = getJsonNodeFromStreamContent(dataInputStream)

    "validate uuid in correct format" in {
      val errors = schema.validate(node)
      println(errors)
      assert(errors.size() === 2)

    }

  }

  def getJsonSchemaFromStreamContentV7(schemaContent: InputStream): JsonSchema = {
    val IRI = SchemaId.V7
    val ID = "$id"


    val myJsonMetaSchema: JsonMetaSchema = JsonMetaSchema
      .builder(IRI)
      .specification(SpecVersion.VersionFlag.V7)
      .idKeyword(ID)
      .formats(Formats.DEFAULT)
      .keywords(
        util.Arrays.asList(
//          new NonValidationKeyword("$schema"),
          new NonValidationKeyword("title"),
          new NonValidationKeyword("description"),
          new NonValidationKeyword("default"),
          new NonValidationKeyword("definitions"),
          new NonValidationKeyword("$comment"),
          new NonValidationKeyword("examples"),
          new NonValidationKeyword("then"),
          new NonValidationKeyword("else"),
          new NonValidationKeyword("additionalItems")
        )
      )
      //.keyword(new GreatValidator)
      .build()

    val sch = JsonMetaSchema.getV7
    sch.getKeywords.put("great",new GreatValidator)

    val factory1 = new JsonSchemaFactory.Builder().defaultMetaSchemaIri(IRI)
      .metaSchema(sch)
      .build();

    val factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)

    val config = new SchemaValidatorsConfig()
    config.setFormatAssertionsEnabled(true)

    factory1.getSchema(schemaContent, config)
  }

  protected def getJsonNodeFromStreamContent(content: InputStream): JsonNode = {
    val mapper = new ObjectMapper()
    mapper.readTree(content)
  }
}
