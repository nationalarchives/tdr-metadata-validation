package uk.gov.nationalarchives.tdr.validation

import uk.gov.nationalarchives.tdr.validation.ErrorCode._

import java.sql.Timestamp
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime, YearMonth}
import scala.util.control.Exception.allCatch

sealed trait DataType

case object Integer extends DataType with Product with Serializable {
  def checkValue(value: String, criteria: MetadataCriteria): Option[String] = {
    value match {
      case "" if criteria.required            => Some(EMPTY_VALUE_ERROR)
      case t if allCatch.opt(t.toInt).isEmpty => Some(NUMBER_ONLY_ERROR)
      case t if t.toInt < 0                   => Some(NEGATIVE_NUMBER_ERROR)
      case t if t.toInt == 0                  => Some(ZERO_NUMBER_ERROR)
      case _                                  => None
    }
  }
}

case object DateTime extends DataType with Product with Serializable {
  def checkValue(value: String, criteria: MetadataCriteria): Option[String] = {
    val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    value match {
      case "" if criteria.required  => Some(EMPTY_VALUE_ERROR)
      case "" if !criteria.required => None
      case v =>
        val dateTime = allCatch.opt(Timestamp.valueOf(v).toLocalDateTime).orElse(allCatch.opt(LocalDate.parse(v, format).atStartOfDay()))
        if (dateTime.isEmpty) {
          val date = v.replaceAll("[T ]", ":").split("[/:]")
          if (date.length < 6) {
            Some(INVALID_DATE_FORMAT_ERROR)
          } else {
            validate(date(2), date(1), date(0), criteria)
          }
        } else {
          dateTime.flatMap(date => validate(date.getDayOfMonth.toString, date.getMonthValue.toString, date.getYear.toString, criteria))
        }
    }
  }

  val isInvalidDay: Int => Boolean = (day: Int) => day < 1 || day > 31
  val isInvalidMonth: Int => Boolean = (month: Int) => month < 1 || month > 12
  val isInvalidYear: Int => Boolean = (year: Int) => year.toString.length != 4

  private def validate(day: String, month: String, year: String, criteria: MetadataCriteria): Option[String] = {
    val emptyDate: Boolean = day.isEmpty && month.isEmpty && year.isEmpty

    emptyDate match {
      case false                     => validateDateValues(day, month, year, criteria)
      case true if criteria.required => Some(EMPTY_VALUE_ERROR)
      case _                         => None
    }
  }

  private def validateDateValues(day: String, month: String, year: String, criteria: MetadataCriteria): Option[String] = {
    validateYear(year) orElse
      validateMonth(month) orElse
      validateDay(day) orElse
      checkDayForTheMonthAndYear(day.toInt, month.toInt, year.toInt) orElse
      checkIfFutureDateIsAllowed(day.toInt, month.toInt, year.toInt, criteria)
  }

  private def validateDay(day: String): Option[String] = {
    day match {
      case v if v.isEmpty                     => Some(EMPTY_VALUE_ERROR_FOR_DAY)
      case v if allCatch.opt(v.toInt).isEmpty => Some(NUMBER_ERROR_FOR_DAY)
      case v if v.toInt < 0                   => Some(NEGATIVE_NUMBER_ERROR_FOR_DAY)
      case v if isInvalidDay(v.toInt)         => Some(INVALID_NUMBER_ERROR_FOR_DAY)
      case _                                  => None
    }
  }

  private def validateMonth(month: String): Option[String] = {
    month match {
      case v if v.isEmpty                     => Some(EMPTY_VALUE_ERROR_FOR_MONTH)
      case v if allCatch.opt(v.toInt).isEmpty => Some(NUMBER_ERROR_FOR_MONTH)
      case v if v.toInt < 0                   => Some(NEGATIVE_NUMBER_ERROR_FOR_MONTH)
      case v if isInvalidMonth(v.toInt)       => Some(INVALID_NUMBER_ERROR_FOR_MONTH)
      case _                                  => None
    }
  }

  private def validateYear(year: String): Option[String] = {
    year match {
      case v if v.isEmpty                     => Some(EMPTY_VALUE_ERROR_FOR_YEAR)
      case v if allCatch.opt(v.toInt).isEmpty => Some(NUMBER_ERROR_FOR_YEAR)
      case v if v.toInt < 0                   => Some(NEGATIVE_NUMBER_ERROR_FOR_YEAR)
      case v if isInvalidYear(v.toInt)        => Some(INVALID_NUMBER_ERROR_FOR_YEAR)
      case _                                  => None
    }
  }

  private def checkDayForTheMonthAndYear(dayNumber: Int, monthNumber: Int, yearNumber: Int): Option[String] = {
    val daysInMonth = YearMonth.of(yearNumber, monthNumber).lengthOfMonth()
    if (dayNumber < 1 || (dayNumber > daysInMonth)) Some(INVALID_DAY_FOR_MONTH_ERROR) else None
  }

  private def checkIfFutureDateIsAllowed(day: Int, month: Int, year: Int, criteria: MetadataCriteria): Option[String] =
    if (!criteria.isFutureDateAllowed && LocalDateTime.now().isBefore(LocalDateTime.of(year, month, day, 0, 0))) {
      Some(FUTURE_DATE_ERROR)
    } else {
      None
    }
}

case object Text extends DataType with Product with Serializable {

  def checkValue(value: String, criteria: MetadataCriteria): Option[String] = {
    val definedValues = criteria.definedValues
    value match {
      case "" if criteria.required                                                                    => Some(EMPTY_VALUE_ERROR)
      case v if criteria.characterLimit.exists(v.length > _)                                          => Some(MAX_CHARACTER_LIMIT_INPUT_ERROR)
      case v if definedValues.nonEmpty && !criteria.isMultiValueAllowed && v.split("[,|]").length > 1 => Some(MULTI_VALUE_ERROR)
      case v if definedValues.nonEmpty && !v.split("[,|]").toList.forall(definedValues.contains)      => Some(UNDEFINED_VALUE_ERROR)
      case _                                                                                          => None
    }
  }
}

case object Boolean extends DataType with Product with Serializable {
  def checkValue(value: String, criteria: MetadataCriteria, requiredMetadata: Option[Metadata]): Option[String] = {
    value match {
      case "" if criteria.required =>
        if (isRequiredMetadataIsEmpty(criteria, requiredMetadata)) {
          None
        } else {
          Some(NO_OPTION_SELECTED_ERROR)
        }
      case v if criteria.requiredProperty.isDefined && requiredMetadata.exists(_.value.isEmpty) => Some(REQUIRED_PROPERTY_IS_EMPTY)
      case v if !criteria.definedValues.contains(v)                                             => Some(UNDEFINED_VALUE_ERROR)
      case _                                                                                    => None
    }
  }

  def isRequiredMetadataIsEmpty(criteria: MetadataCriteria, requiredMetadata: Option[Metadata]): Boolean = {
    criteria.requiredProperty.isDefined && requiredMetadata.exists(_.value.isEmpty)
  }
}
case object Decimal extends DataType with Product with Serializable

object DataType {
  def get(dataType: String): DataType = {
    dataType match {
      case "Integer"  => Integer
      case "DateTime" => DateTime
      case "Text"     => Text
      case "Boolean"  => Boolean
      case "Decimal"  => Decimal
    }
  }
}
