package uk.gov.nationalarchives.tdr.validation.schema

import com.fasterxml.jackson.databind.JsonNode
import com.networknt.schema._

import java.util
import java.util.Collections


class FileExistsValidator extends AbstractKeyword("fileExists") {


  override def newValidator(schemaLocation: SchemaLocation, evaluationPath: JsonNodePath, schemaNode: JsonNode, parentSchema: JsonSchema, validationContext: ValidationContext): JsonValidator = {

     val config = schemaNode.asText
    new AbstractJsonValidator(schemaLocation, evaluationPath, this,schemaNode) {

      override def validate(executionContext: ExecutionContext, node: JsonNode, rootNode: JsonNode, instanceLocation: JsonNodePath): util.Set[ValidationMessage] = {
        println("checking file exists")
        val set:util.HashSet[ValidationMessage] = new util.HashSet[ValidationMessage]
        val v = new ValidationMessage("checking file exists")
        set.add(v)
        set
      }
    }

  }
}
