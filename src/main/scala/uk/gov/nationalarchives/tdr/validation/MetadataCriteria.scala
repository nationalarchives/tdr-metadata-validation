package uk.gov.nationalarchives.tdr.validation

case class Metadata(name: String, value: String)
case class MetadataCriteria(
    name: String,
    dataType: DataType,
    required: Boolean,
    isFutureDateAllowed: Boolean,
    isMultiValueAllowed: Boolean,
    definedValues: List[String],
    requiredProperty: Option[String] = None,
    dependencies: Option[Map[String, List[MetadataCriteria]]] = None,
    defaultValue: Option[String] = None
)

object MetadataProperty {
  val closureType = "ClosureType"
  val descriptiveType = "DescriptiveType"
}

object ErrorCode {
  val CLOSURE_STATUS_IS_MISSING = "CLOSURE_STATUS_IS_MISSING"
  val CLOSURE_METADATA_EXISTS_WHEN_FILE_IS_OPEN = "CLOSURE_METADATA_EXISTS_WHEN_FILE_IS_OPEN"
  val NUMBER_ONLY_ERROR = "NUMBER_ONLY_ERROR"
  val NEGATIVE_NUMBER_ERROR = "NEGATIVE_NUMBER_ERROR"
  val EMPTY_VALUE_ERROR = "EMPTY_VALUE_ERROR"
  val NO_OPTION_SELECTED_ERROR = "NO_OPTION_SELECTED_ERROR"
  val INVALID_DATE_FORMAT_ERROR = "INVALID_DATE_FORMAT_ERROR"
  val INVALID_NUMBER_ERROR = "INVALID_NUMBER_ERROR"
  val EMPTY_VALUE_ERROR_FOR_DAY = "EMPTY_VALUE_ERROR_FOR_DAY"
  val NUMBER_ERROR_FOR_DAY = "NUMBER_ERROR_FOR_DAY"
  val NEGATIVE_NUMBER_ERROR_FOR_DAY = "NEGATIVE_NUMBER_ERROR_FOR_DAY"
  val INVALID_NUMBER_ERROR_FOR_DAY = "INVALID_NUMBER_ERROR_FOR_DAY"
  val EMPTY_VALUE_ERROR_FOR_MONTH = "EMPTY_VALUE_ERROR_FOR_MONTH"
  val NUMBER_ERROR_FOR_MONTH = "NUMBER_ERROR_FOR_MONTH"
  val NEGATIVE_NUMBER_ERROR_FOR_MONTH = "NEGATIVE_NUMBER_ERROR_FOR_MONTH"
  val INVALID_NUMBER_ERROR_FOR_MONTH = "INVALID_NUMBER_ERROR_FOR_MONTH"
  val EMPTY_VALUE_ERROR_FOR_YEAR = "EMPTY_VALUE_ERROR_FOR_YEAR"
  val NUMBER_ERROR_FOR_YEAR = "NUMBER_ERROR_FOR_YEAR"
  val NEGATIVE_NUMBER_ERROR_FOR_YEAR = "NEGATIVE_NUMBER_ERROR_FOR_YEAR"
  val INVALID_NUMBER_ERROR_FOR_YEAR = "INVALID_NUMBER_ERROR_FOR_YEAR"
  val INVALID_DAY_FOR_MONTH_ERROR = "INVALID_DAY_FOR_MONTH_ERROR"
  val FUTURE_DATE_ERROR = "FUTURE_DATE_ERROR"
  val MULTI_VALUE_ERROR = "MULTI_VALUE_ERROR"
  val UNDEFINED_VALUE_ERROR = "UNDEFINED_VALUE_ERROR"
  val REQUIRED_PROPERTY_IS_EMPTY = "REQUIRED_PROPERTY_IS_EMPTY"
}
