package uk.gov.nationalarchives.tdr.validation

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.nationalarchives.tdr.validation.ErrorCode._

class DataTypeSpec extends AnyWordSpec {

  "Integer" should {

    val criteria = MetadataCriteria("Property1", Integer, required = true, isFutureDateAllowed = false, isMultiValueAllowed = false, Nil, None, None)

    "checkValue should return an error if the value is empty" in {
      Integer.checkValue("", criteria) should be(Some(EMPTY_VALUE_ERROR))
    }

    "checkValue should return an error if the value is not an integer" in {
      Integer.checkValue("e", criteria) should be(Some(NUMBER_ONLY_ERROR))
    }

    "checkValue should return an error if the value is a negative number" in {
      Integer.checkValue("-1", criteria) should be(Some(NEGATIVE_NUMBER_ERROR))
    }

    "checkValue should not return any errors if the value is a valid number" in {
      Integer.checkValue("1", criteria) should be(None)
    }
  }

  "DateTime" should {

    val criteria = MetadataCriteria("Property1", DateTime, required = true, isFutureDateAllowed = false, isMultiValueAllowed = false, Nil, None, None)

    "checkValue should not return any errors if the date is valid" in {
      DateTime.checkValue("1990/12/10T00:00:00", criteria) should be(None)
      DateTime.checkValue("2000/2/29T00:00:00", criteria) should be(None)
      DateTime.checkValue("1990/01/01 00:00:00.0", criteria) should be(None)
      DateTime.checkValue("1990:01:01T00:00:00.0", criteria) should be(None)
    }

    "checkValue should not return any errors if the value is empty but it is not mandatory" in {
      DateTime.checkValue("", criteria.copy(required = false)) should be(None)
    }

    "checkValue should not return any errors if future date is allowed" in {
      DateTime.checkValue("1990/12/10T00:00:00", criteria.copy(isFutureDateAllowed = true)) should be(None)
      DateTime.checkValue("2090/12/10T00:00:00", criteria.copy(isFutureDateAllowed = true)) should be(None)
      DateTime.checkValue("2090/12/10 00:00:00", criteria.copy(isFutureDateAllowed = true)) should be(None)
    }

    "checkValue should return an error if the date format is invalid" in {
      DateTime.checkValue("1990-12-10T00:00:00", criteria) should be(Some(INVALID_DATE_FORMAT_ERROR))
      DateTime.checkValue("2000/2/29", criteria) should be(Some(INVALID_DATE_FORMAT_ERROR))
    }

    "checkValue should return an error if the day/month/year in negative" in {
      DateTime.checkValue("1990:12:-10T00:00:00", criteria) should be(Some(NEGATIVE_NUMBER_ERROR_FOR_DAY))
      DateTime.checkValue("1990:-12:10T00:00:00", criteria) should be(Some(NEGATIVE_NUMBER_ERROR_FOR_MONTH))
      DateTime.checkValue("-1990:12:10T00:00:00", criteria) should be(Some(NEGATIVE_NUMBER_ERROR_FOR_YEAR))
    }

    "checkValue should return an error if the value is empty and mandatory" in {
      DateTime.checkValue("", criteria) should be(Some(EMPTY_VALUE_ERROR))
      DateTime.checkValue("//T00:00:00", criteria) should be(Some(EMPTY_VALUE_ERROR))
    }

    "checkValue should return an error if the day or month or year is empty" in {
      DateTime.checkValue("1990/10/T00:00:00", criteria) should be(Some(EMPTY_VALUE_ERROR_FOR_DAY))
      DateTime.checkValue("1990//10T00:00:00", criteria) should be(Some(EMPTY_VALUE_ERROR_FOR_MONTH))
      DateTime.checkValue("/10/10T00:00:00", criteria) should be(Some(EMPTY_VALUE_ERROR_FOR_YEAR))
      DateTime.checkValue("/10/10 00:00:00", criteria) should be(Some(EMPTY_VALUE_ERROR_FOR_YEAR))
    }

    "checkValue should return an error if the day or month or year is not valid" in {
      DateTime.checkValue("1990/10/32T00:00:00", criteria) should be(Some(INVALID_NUMBER_ERROR_FOR_DAY))
      DateTime.checkValue("1990/28/10T00:00:00", criteria) should be(Some(INVALID_NUMBER_ERROR_FOR_MONTH))
      DateTime.checkValue("19999/10/10T00:00:00", criteria) should be(Some(INVALID_NUMBER_ERROR_FOR_YEAR))
      DateTime.checkValue("199/10/10T00:00:00", criteria) should be(Some(INVALID_NUMBER_ERROR_FOR_YEAR))
      DateTime.checkValue("199/10/10 00:00:00", criteria) should be(Some(INVALID_NUMBER_ERROR_FOR_YEAR))
    }

    "checkValue should return an error if the day is not valid for given month" in {
      DateTime.checkValue("1990/2/29T00:00:00", criteria) should be(Some(INVALID_DAY_FOR_MONTH_ERROR))
      DateTime.checkValue("1990/4/31T00:00:00", criteria) should be(Some(INVALID_DAY_FOR_MONTH_ERROR))
      DateTime.checkValue("1990/6/31T00:00:00", criteria) should be(Some(INVALID_DAY_FOR_MONTH_ERROR))
      DateTime.checkValue("1990/9/31T00:00:00", criteria) should be(Some(INVALID_DAY_FOR_MONTH_ERROR))
      DateTime.checkValue("1990/11/31T00:00:00", criteria) should be(Some(INVALID_DAY_FOR_MONTH_ERROR))
      DateTime.checkValue("1990/11/31 00:00:00", criteria) should be(Some(INVALID_DAY_FOR_MONTH_ERROR))
    }

    "checkValue should return an error if future date is not allowed" in {
      DateTime.checkValue("2050/2/1T00:00:00", criteria) should be(Some(FUTURE_DATE_ERROR))
    }
  }

  "Text" should {

    val criteria = MetadataCriteria("Property1", Text, required = true, isFutureDateAllowed = false, isMultiValueAllowed = false, Nil, None, None, None, Some(5))

    "checkValue should not return any errors if the value is valid" in {
      Text.checkValue("hello", criteria) should be(None)
    }

    "checkValue should not return any errors if the value is empty but it is not mandatory" in {
      Text.checkValue("", criteria.copy(required = false)) should be(None)
    }

    "checkValue should return an error if the value is empty" in {
      Text.checkValue("", criteria) should be(Some(EMPTY_VALUE_ERROR))
    }

    "checkValue should not return any errors if the value has multiple values but multiple values are allowed" in {
      Text.checkValue("22,44", criteria.copy(definedValues = List("22", "44"), isMultiValueAllowed = true)) should be(None)
    }

    "checkValue should return an error if the value has multiple values but multiple values are not allowed" in {
      Text.checkValue("22,44", criteria.copy(definedValues = List("22", "44"))) should be(Some(MULTI_VALUE_ERROR))
    }

    "checkValue should return an error if the value is not matching its defined values" in {
      Text.checkValue("22,44", criteria.copy(definedValues = List("22", "33"), isMultiValueAllowed = true)) should be(Some(UNDEFINED_VALUE_ERROR))
    }

    "checkValue should return an error if the length of the value is greater than the defined character limit" in {
      Text.checkValue("123456", criteria) should be(Some(MAX_CHARACTER_LIMIT_INPUT_ERROR))
    }
  }

  "Boolean" should {

    val criteria = MetadataCriteria("Property1", Boolean, required = true, isFutureDateAllowed = false, isMultiValueAllowed = false, List("yes", "no"), None, None)

    "checkValue should not return any errors if the value is valid" in {
      Boolean.checkValue("yes", criteria, None) should be(None)
    }

    "checkValue should not return any errors if the value is empty but required property is empty too" in {
      Boolean.checkValue("", criteria.copy(requiredProperty = Some("property2")), Some(Metadata("property2", ""))) should be(None)
    }

    "checkValue should return an error if the value is empty and no required property" in {
      Boolean.checkValue("", criteria, None) should be(Some(NO_OPTION_SELECTED_ERROR))
    }

    "checkValue should return an error if the value is empty and has required property" in {
      Boolean.checkValue("", criteria.copy(requiredProperty = Some("property2")), Some(Metadata("property2", "value"))) should be(Some(NO_OPTION_SELECTED_ERROR))
    }

    "checkValue should return an error if the value doesn't match its defined values" in {
      Boolean.checkValue("true", criteria, None) should be(Some(UNDEFINED_VALUE_ERROR))
    }

    "checkValue should return an error if the required property value is empty" in {
      Boolean.checkValue("yes", criteria.copy(requiredProperty = Some("property2")), Some(Metadata("property2", ""))) should be(Some(REQUIRED_PROPERTY_IS_EMPTY))
    }
  }
}
