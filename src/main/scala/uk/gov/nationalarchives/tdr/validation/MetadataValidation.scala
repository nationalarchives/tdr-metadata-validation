package uk.gov.nationalarchives.tdr.validation

import org.apache.commons.lang3.BooleanUtils
import uk.gov.nationalarchives.tdr.validation.ErrorCode._
import uk.gov.nationalarchives.tdr.validation.MetadataProperty._

case class FileRow(fileName: String, metadata: List[Metadata])
case class Error(propertyName: String, errorCode: String)

class MetadataValidation(closureMetadataCriteria: MetadataCriteria, descriptiveMetadataCriteria: List[MetadataCriteria]) extends scala.Serializable {

  def validateMetadata(fileRows: List[FileRow]): Map[String, List[Error]] = {
    fileRows.map(row => row.fileName -> (validateClosureMetadata(row.metadata) ++ validateDescriptiveMetadata(row.metadata))).toMap
  }

  def validateClosureMetadata(input: List[Metadata]): List[Error] = {
    val closureStatus = input.find(_.name == closureType)
    closureStatus match {
      case Some(Metadata(_, "Open")) =>
        val hasAnyClosureMetadata = hasClosureMetadata(input, closureMetadataCriteria.dependencies.flatMap(_.get("Closed")))
        if (hasAnyClosureMetadata) {
          List(Error(closureType, CLOSURE_METADATA_EXISTS_WHEN_FILE_IS_OPEN))
        } else {
          List.empty
        }
      case Some(Metadata(_, "Closed")) => validateMetadata(input, closureMetadataCriteria.dependencies.flatMap(_.get("Closed")).getOrElse(Nil))
      case Some(Metadata(_, _))        => List(Error(closureType, UNDEFINED_VALUE_ERROR))
      case None                        => List(Error(closureType, CLOSURE_STATUS_IS_MISSING))
    }
  }

  def hasClosureMetadata(input: List[Metadata], metadataCriteria: Option[List[MetadataCriteria]]): Boolean = {
    metadataCriteria.exists(
      _.exists(criteria =>
        input.find(_.name == criteria.name).exists(_.value != criteria.defaultValue.map(_.toYesOrNo).getOrElse(""))
          || hasClosureMetadata(input, criteria.dependencies.flatMap(_.get(criteria.defaultValue.getOrElse(""))))
      )
    )
  }

  implicit class BooleanValue(value: String) {
    def toYesOrNo: String = {
      value match {
        case "true"  => "Yes"
        case "false" => "No"
        case _       => value
      }
    }
  }

  def validateDescriptiveMetadata(input: List[Metadata]): List[Error] = validateMetadata(input, descriptiveMetadataCriteria)

  private def validateMetadata(input: List[Metadata], metadataCriteria: List[MetadataCriteria]): List[Error] = {
    input.flatMap(metadata => {
      metadataCriteria
        .find(_.name == metadata.name)
        .flatMap(criteria => {
          val value = metadata.value
          val errorCode = criteria.dataType match {
            case Integer | Decimal => Integer.checkValue(value, criteria)
            case Boolean =>
              Boolean.checkValue(value, criteria, criteria.requiredProperty.flatMap(p => input.find(_.name == p))) match {
                case None if value.nonEmpty =>
                  criteria.dependencies
                    .flatMap(
                      _.collect {
                        case (definedValue, criteria) if BooleanUtils.toBoolean(definedValue) == BooleanUtils.toBoolean(value) =>
                          validateMetadata(input.filter(r => criteria.exists(_.name == r.name)), criteria.map(_.copy(required = true))).map(_.errorCode).headOption
                      }.head
                    )
                case error => error
              }
            case Text     => Text.checkValue(value, criteria)
            case DateTime => DateTime.checkValue(value, criteria)
            case _        => None
          }
          errorCode.map(Error(criteria.name, _))
        })
    })
  }
}
