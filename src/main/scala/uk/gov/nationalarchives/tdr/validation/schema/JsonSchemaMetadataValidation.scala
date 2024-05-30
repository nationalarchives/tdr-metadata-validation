package uk.gov.nationalarchives.tdr.validation.schema

import com.networknt.schema.ValidationMessage
import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaDefinition.BASE_SCHEMA
import uk.gov.nationalarchives.tdr.validation.{Error, FileRow}

object JsonSchemaMetadataValidation {

  def validateMetadata(jsonSchemaDefinition: JsonSchemaDefinition, fileRows: List[FileRow]): Map[String, List[ValidationMessage]] = {
    jsonSchemaDefinition match {
      case BASE_SCHEMA =>
        fileRows
          .map(fileRow =>
            fileRow.fileName ->
              JsonSchemaValidators.validateJson(jsonSchemaDefinition, convertToJson(fileRow)).toList
          )
          .toMap
    }
  }

  private def convertToJson(fileRow: FileRow) = {
    fileRow.metadata.map(data => (data.name -> data.value))
    val json =
      """{
          "date_last_modified":"2067-12-23",
          "foi_exemption_code": [
             "23"
           ],
           "language" : ["Banana"]
      }"""
    json
  }
}
