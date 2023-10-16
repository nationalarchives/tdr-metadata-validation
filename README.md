# TDR Metadata Validation

This validation library is used to validate closure & descriptive metadata as per the provided metadata criteria.
If the input values have any errors then it will return a list of error codes. You can use the error codes to render the client's specific error messages.

## How to use?

1. Create `MetadataCriteria` for closure or descriptive metadata
2. Create a `MetadataValidation` object with the criteria
3. Now call the validation method (closure or descriptive) with your metadata

Example:
```scala
import uk.gov.nationalarchives.tdr.validation._

val closureMetadataCriteria = MetadataCriteria("ClosureType", Boolean, true, false, false, List("yes", "no"))
val descriptiveMetadataCriteria = List(
  MetadataCriteria("Property1", Text, false, false, false, Nil, None, None),
)
val metadataValidation = new MetadataValidation(closureMetadataCriteria, descriptiveMetadataCriteria)

val metadata = List(
  Metadata("Property1", ""),
  Metadata("Property2", ""),
  Metadata("Property3", "test")
)

val closureMetadataErrors = metadataValidation.validateClosureMetadata(metadata)
val descriptiveMetadataErrors = metadataValidation.validateDescriptiveMetadata(metadata)
```
