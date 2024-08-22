package uk.gov.nationalarchives.tdr.validation

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import uk.gov.nationalarchives.tdr.validation.ErrorCode._
import uk.gov.nationalarchives.tdr.validation.MetadataProperty.closureType

class MetadataValidationSpec extends AnyFlatSpec {

  "validateClosureMetadata" should "validate closure metadata" in {

    val dependentMetadataCriteria = List(
      MetadataCriteria("Property1", Boolean, true, false, false, List("yes", "no"), None, None),
      MetadataCriteria("Property2", Text, true, false, false, Nil, None, None),
      MetadataCriteria("Property3", DateTime, true, false, false, Nil, None, None),
      MetadataCriteria("Property4", Integer, true, false, false, Nil, None, None)
    )
    val closureMetadataCriteria = MetadataCriteria("ClosureType", Boolean, true, false, false, List("yes", "no"), None, Some(Map("Closed" -> dependentMetadataCriteria)), None)
    val input = List(
      Metadata("ClosureType", "Closed"),
      Metadata("Property1", "yes"),
      Metadata("Property2", "hello"),
      Metadata("Property3", "1990/10/10T00:00:00"),
      Metadata("Property4", "90")
    )
    val metadataValidation = new MetadataValidation(closureMetadataCriteria, Nil)
    val error = metadataValidation.validateClosureMetadata(input)
    error should be(Nil)
  }

  "validateClosureMetadata" should "return an error if closure type is empty" in {

    val dependentMetadataCriteria = List(
      MetadataCriteria("Property1", Boolean, true, false, false, List("yes", "no"), None, None)
    )
    val closureMetadataCriteria = MetadataCriteria("ClosureType", Boolean, true, false, false, List("yes", "no"), None, Some(Map("Closed" -> dependentMetadataCriteria)), None)
    val input = List(
      Metadata("ClosureType", ""),
      Metadata("Property1", "yes")
    )
    val metadataValidation = new MetadataValidation(closureMetadataCriteria, Nil)
    val error = metadataValidation.validateClosureMetadata(input)
    error should be(List(Error(closureType, UNDEFINED_VALUE_ERROR)))
  }

  "validateClosureMetadata" should "return an error if closure type is missing" in {

    val dependentMetadataCriteria = List(
      MetadataCriteria("Property1", Boolean, true, false, false, List("yes", "no"), None, None)
    )
    val closureMetadataCriteria = MetadataCriteria("ClosureType", Boolean, true, false, false, List("yes", "no"), None, Some(Map("Closed" -> dependentMetadataCriteria)), None)
    val input = List(
      Metadata("Property1", "yes")
    )
    val metadataValidation = new MetadataValidation(closureMetadataCriteria, Nil)
    val error = metadataValidation.validateClosureMetadata(input)
    error should be(List(Error(closureType, CLOSURE_STATUS_IS_MISSING)))
  }

  "validateClosureMetadata" should "not return any errors if closure status is open and closure metadata has empty or default values" in {

    val dependentMetadataCriteria = List(
      MetadataCriteria("Property1", Boolean, true, false, false, List("yes", "no"), defaultValue = Some("no")),
      MetadataCriteria("Property2", Text, true, false, false, Nil, None, None)
    )
    val closureMetadataCriteria = MetadataCriteria("ClosureType", Boolean, true, false, false, List("yes", "no"), None, Some(Map("Closed" -> dependentMetadataCriteria)), None)
    val input = List(
      Metadata("ClosureType", "Open"),
      Metadata("Property1", "no"),
      Metadata("Property2", "")
    )
    val metadataValidation = new MetadataValidation(closureMetadataCriteria, Nil)
    val error = metadataValidation.validateClosureMetadata(input)
    error should be(Nil)
  }

  "validateClosureMetadata" should "return an error if closure status is open but has closure metadata" in {

    val dependentMetadataCriteria = List(
      MetadataCriteria("Property1", Boolean, true, false, false, List("yes", "no"), None, None),
      MetadataCriteria("Property2", Text, true, false, false, Nil, None, None)
    )
    val closureMetadataCriteria = MetadataCriteria("ClosureType", Boolean, true, false, false, List("yes", "no"), None, Some(Map("Closed" -> dependentMetadataCriteria)), None)
    val input = List(
      Metadata("ClosureType", "Open"),
      Metadata("Property1", "yes"),
      Metadata("Property2", "hello")
    )
    val metadataValidation = new MetadataValidation(closureMetadataCriteria, Nil)
    val error = metadataValidation.validateClosureMetadata(input)
    error should be(List(Error(closureType, CLOSURE_METADATA_EXISTS_WHEN_FILE_IS_OPEN)))
  }

  "validateClosureMetadata" should "return an error if closure metadata has invalid values" in {

    val dependentMetadataCriteria = List(
      MetadataCriteria("Property1", Boolean, true, false, false, List("yes", "no"), None, None),
      MetadataCriteria("Property2", Text, true, false, false, Nil, None, None),
      MetadataCriteria("Property3", DateTime, true, false, false, Nil, None, None),
      MetadataCriteria("Property4", Integer, true, false, false, Nil, None, None)
    )
    val closureMetadataCriteria = MetadataCriteria("ClosureType", Boolean, true, false, false, List("yes", "no"), None, Some(Map("Closed" -> dependentMetadataCriteria)), None)
    val input = List(
      Metadata("ClosureType", "Closed"),
      Metadata("Property1", ""),
      Metadata("Property2", ""),
      Metadata("Property3", "1990/55/10T00:00:00"),
      Metadata("Property4", "tt")
    )
    val metadataValidation = new MetadataValidation(closureMetadataCriteria, Nil)
    val error = metadataValidation.validateClosureMetadata(input)
    error should be(
      List(
        Error("Property1", NO_OPTION_SELECTED_ERROR),
        Error("Property2", EMPTY_VALUE_ERROR),
        Error("Property3", INVALID_NUMBER_ERROR_FOR_MONTH),
        Error("Property4", NUMBER_ONLY_ERROR)
      )
    )
  }

  "validateClosureMetadata" should "return an error if dependent closure metadata has invalid values" in {

    val dependentMetadataCriteria = List(
      MetadataCriteria(
        "Property1",
        Boolean,
        true,
        false,
        false,
        List("yes", "no"),
        dependencies = Some(Map("yes" -> List(MetadataCriteria("Property11", Text, true, false, false, Nil))))
      ),
      MetadataCriteria("Property2", Text, true, false, false, Nil, None, None),
      MetadataCriteria("Property4", Integer, true, false, false, Nil, None, None)
    )
    val closureMetadataCriteria = MetadataCriteria("ClosureType", Boolean, true, false, false, List("yes", "no"), None, Some(Map("Closed" -> dependentMetadataCriteria)), None)
    val input = List(
      Metadata("ClosureType", "Closed"),
      Metadata("Property1", "yes"),
      Metadata("Property11", ""),
      Metadata("Property2", ""),
      Metadata("Property4", "")
    )
    val metadataValidation = new MetadataValidation(closureMetadataCriteria, Nil)
    val error = metadataValidation.validateClosureMetadata(input)
    error should be(
      List(
        Error("Property1", EMPTY_VALUE_ERROR),
        Error("Property2", EMPTY_VALUE_ERROR),
        Error("Property4", EMPTY_VALUE_ERROR)
      )
    )
  }

  "validateClosureMetadata" should "not return an error if closure metadata is dependent on descriptive metadata which is empty" in {

    val dependentMetadataCriteria = List(
      MetadataCriteria(
        "Property1",
        Boolean,
        true,
        false,
        false,
        List("yes", "no"),
        requiredProperty = Some("Property5"),
        dependencies = Some(Map("yes" -> List(MetadataCriteria("Property11", Text, true, false, false, Nil))))
      )
    )
    val closureMetadataCriteria = MetadataCriteria("ClosureType", Boolean, true, false, false, List("yes", "no"), None, Some(Map("Closed" -> dependentMetadataCriteria)), None)
    val input = List(
      Metadata("ClosureType", "Closed"),
      Metadata("Property1", ""),
      Metadata("Property11", ""),
      Metadata("Property5", "")
    )
    val metadataValidation = new MetadataValidation(closureMetadataCriteria, Nil)
    val error = metadataValidation.validateClosureMetadata(input)
    error should be(Nil)
  }

  "validateDescriptiveMetadata" should "validate descriptive metadata" in {

    val descriptiveMetadataCriteria = List(
      MetadataCriteria("Property1", Text, false, false, false, Nil, None, None),
      MetadataCriteria("Property2", Text, false, false, false, Nil, None, None),
      MetadataCriteria("Property3", Text, false, false, false, Nil, None, None)
    )
    val closureMetadataCriteria = MetadataCriteria("ClosureType", Boolean, true, false, false, List("yes", "no"))
    val input = List(
      Metadata("Property1", ""),
      Metadata("Property2", ""),
      Metadata("Property3", "test")
    )
    val metadataValidation = new MetadataValidation(closureMetadataCriteria, descriptiveMetadataCriteria)
    val error = metadataValidation.validateDescriptiveMetadata(input)
    error should be(Nil)
  }

  "validateMetadata" should "validate closure and descriptive metadata" in {

    val descriptiveMetadataCriteria = List(
      MetadataCriteria("Property21", Text, false, false, false, Nil, None, None),
      MetadataCriteria("Property22", Boolean, false, false, false, List("yes", "no"), None, None)
    )
    val dependentMetadataCriteria = List(
      MetadataCriteria("Property1", Boolean, true, false, false, List("yes", "no"), None, None),
      MetadataCriteria("Property2", Text, true, false, false, Nil, None, None)
    )
    val closureMetadataCriteria = MetadataCriteria("ClosureType", Boolean, true, false, false, List("yes", "no"), None, Some(Map("Closed" -> dependentMetadataCriteria)), None)
    val metadata = List(
      Metadata("ClosureType", "Closed"),
      Metadata("Property1", ""),
      Metadata("Property2", ""),
      Metadata("Property21", ""),
      Metadata("Property22", "hh")
    )
    val fileRows = FileRow("file1", metadata)
    val metadataValidation = new MetadataValidation(closureMetadataCriteria, descriptiveMetadataCriteria)
    val error = metadataValidation.validateMetadata(List(fileRows))
    error should be(
      Map(
        "file1" -> List(
          Error("Property1", NO_OPTION_SELECTED_ERROR),
          Error("Property2", EMPTY_VALUE_ERROR),
          Error("Property22", UNDEFINED_VALUE_ERROR)
        )
      )
    )
  }
}
