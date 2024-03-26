package uk.gov.nationalarchives.tdr.validation

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.networknt.schema._
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.nationalarchives.tdr.validation.schema.{FileExistsValidator, InThePastValidator}

import java.io.InputStream


class SchemaDataTypeSpec extends AnyWordSpec {

  "JSON schema validation" should {

    val schemaPath = "/schema/closureSchema.schema.json"
    val dataPath = "/data/testData.json"
    val schemaInputStream = getClass.getResourceAsStream(schemaPath)
    val schema = getJsonSchemaFromStreamContentV7(schemaInputStream)
    val dataInputStream = getClass.getResourceAsStream(dataPath)
    val node = getJsonNodeFromStreamContent(dataInputStream)

    "validate uuid in correct format" in {
      val errors = schema.validate(node)
      println(errors)
      assert(errors.size() === 3)

    }

  }

  def getJsonSchemaFromStreamContentV7(schemaContent: InputStream): JsonSchema = {
    val IRI = SchemaId.V7


    val sch = JsonMetaSchema.getV7
    sch.getKeywords.put("inThePast", new InThePastValidator)
    sch.getKeywords.put("fileExists", new FileExistsValidator)
    val factory1 = new JsonSchemaFactory.Builder()
      .defaultMetaSchemaIri(IRI)
      .metaSchema(sch)
      .build()

    val config = new SchemaValidatorsConfig()
    config.setFormatAssertionsEnabled(true)

    factory1.getSchema(schemaContent, config)
  }

  protected def getJsonNodeFromStreamContent(content: InputStream): JsonNode = {
    val mapper = new ObjectMapper()
    mapper.readTree(content)
  }
}
