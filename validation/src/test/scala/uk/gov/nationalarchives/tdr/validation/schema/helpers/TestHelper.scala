package uk.gov.nationalarchives.tdr.validation.schema.helpers

import uk.gov.nationalarchives.tdr.validation.schema.JsonSchemaDefinition.{BASE_SCHEMA, CLOSURE_SCHEMA_CLOSED, CLOSURE_SCHEMA_OPEN, REQUIRED_SCHEMA}
import uk.gov.nationalarchives.tdr.validation.schema.{MetadataValidationJsonSchema, ValidationError}
import uk.gov.nationalarchives.tdr.validation.{FileRow, Metadata}

object TestHelper {

  def openMetadataFileRow(
      filePath: Option[String] = Some("content/file1"),
      dateOfRecord: Option[String] = Some(""),
      description: Option[String] = Some(""),
      closureType: Option[String] = Some("Open"),
      closurePeriod: Option[String] = Some(""),
      closureStartDate: Option[String] = Some(""),
      descriptionClosed: Option[String] = Some("No"),
      foiExemptionAsserted: Option[String] = Some(""),
      foiCodes: Option[String] = Some(""),
      titleClosed: Option[String] = Some("No"),
      titleAlternative: Option[String] = Some(""),
      descriptionAlternative: Option[String] = Some(""),
      //not required
      fileName: Option[String] = Some("file1"),
      dateLastModified: Option[String] = Some("2024-12-25"),
      formerReference: Option[String] = Some(""),
      language: Option[String] = Some("English"),
      translatedTitleOfRecord: Option[String] = Some(""),
    ): FileRow = {
    metadataFileRowBuilder(
      filePath = filePath,
      dateOfRecord = dateOfRecord,
      description = description,
      closureType = closureType,
      closurePeriod = closurePeriod,
      closureStartDate = closureStartDate,
      descriptionClosed = descriptionClosed,
      foiExemptionAsserted = foiExemptionAsserted,
      foiCodes = foiCodes,
      titleClosed = titleClosed,
      titleAlternative = titleAlternative,
      descriptionAlternative = descriptionAlternative,
      //not required
      fileName = fileName,
      dateLastModified= dateLastModified,
      formerReference= formerReference,
      language = language,
      translatedTitleOfRecord= translatedTitleOfRecord,
    )
  }


  def closedMetadataFileRow(
    filePath: Option[String] = Some("content/file1"),
    dateOfRecord: Option[String] = Some(""),
    description: Option[String] = Some(""),
    closureType: Option[String] = Some("Closed"),
    closurePeriod: Option[String] = Some("50"),
    closureStartDate: Option[String] = Some("2024-12-31"),
    descriptionClosed: Option[String] = Some("No"),
    foiExemptionAsserted: Option[String] = Some("2025-01-31"),
    foiCodes: Option[String] = Some("44"),
    titleClosed: Option[String] = Some("No"),
    titleAlternative: Option[String] = Some(""),
    descriptionAlternative: Option[String] = Some(""),
    //not required
    fileName: Option[String] = Some("file1"),
    dateLastModified: Option[String] = Some("2024-12-25"),
    formerReference: Option[String] = Some(""),
    language: Option[String] = Some("English"),
    translatedTitleOfRecord: Option[String] = Some(""),
  ): FileRow = {
    metadataFileRowBuilder(
      filePath = filePath,
      dateOfRecord = dateOfRecord,
      description = description,
      closureType = closureType,
      closurePeriod = closurePeriod,
      closureStartDate = closureStartDate,
      descriptionClosed = descriptionClosed,
      foiExemptionAsserted = foiExemptionAsserted,
      foiCodes = foiCodes,
      titleClosed = titleClosed,
      titleAlternative = titleAlternative,
      descriptionAlternative = descriptionAlternative,
      //not required
      fileName = fileName,
      dateLastModified= dateLastModified,
      formerReference= formerReference,
      language = language,
      translatedTitleOfRecord= translatedTitleOfRecord,
    )
  }


  private def metadataFileRowBuilder(
       //required
       filePath: Option[String] = None,
       dateOfRecord: Option[String] = None,
       description:  Option[String] = None,
       closureType: Option[String] = None,
       closurePeriod: Option[String] = None,
       closureStartDate: Option[String] = None,
       descriptionClosed: Option[String] = None,
       foiExemptionAsserted: Option[String] = None,
       foiCodes: Option[String] = None,
       titleClosed: Option[String] = None,
       titleAlternative: Option[String] = None,
       descriptionAlternative: Option[String] = None,
       //not required
       fileName: Option[String] = None,
       dateLastModified: Option[String] = None,
       formerReference: Option[String] = None,
       language: Option[String] = None,
       translatedTitleOfRecord: Option[String] = None,
     ): FileRow = {
    val metadata = List(
      //required
      filePath.map(Metadata("Filepath", _)), // file_path
      dateOfRecord.map(Metadata("Date of the record", _)), // end_date
      description.map(Metadata("Description", _)), // description
      closureType.map(Metadata("Closure status", _)), // closure_type
      closurePeriod.map(Metadata("Closure Period", _)), // closure_period
      closureStartDate.map(Metadata("Closure Start Date", _)), // closure_start_date
      descriptionClosed.map(Metadata("Is the description sensitive for the public?", _)), // description_closed
      foiExemptionAsserted.map(Metadata("FOI decision asserted", _)), // foi_exemption_asserted
      foiCodes.map(Metadata("FOI exemption code", _)), // foi_exemption_code
      titleClosed.map(Metadata("Is the title sensitive for the public?", _)), // title_closed
      titleAlternative.map(Metadata("Add alternative title without the file extension", _)), // title_alternative
      descriptionAlternative.map(Metadata("Alternative description", _)), // description_alternate
      //not required
      fileName.map(Metadata("Filename", _)),
      dateLastModified.map(Metadata("Date last modified", _)),
      formerReference.map(Metadata("Former reference", _)),
      language.map(Metadata("Language", _)),
      translatedTitleOfRecord.map(Metadata("Translated title of record", _))
    ).flatten
    FileRow("file1", metadata)
  }

  def validationErrors(testFileRow: FileRow): Seq[ValidationError] = {
    val requiredSchemaErrors = MetadataValidationJsonSchema.validate(Set(REQUIRED_SCHEMA), Seq(testFileRow))
    if(requiredSchemaErrors("file1").nonEmpty) {
      requiredSchemaErrors("file1")
    } else {
      val validationErrors: Map[String, Seq[ValidationError]] =  MetadataValidationJsonSchema.validate(Set(REQUIRED_SCHEMA, BASE_SCHEMA, CLOSURE_SCHEMA_CLOSED, CLOSURE_SCHEMA_OPEN), Seq(testFileRow))
      validationErrors("file1")
    }
  }

}
