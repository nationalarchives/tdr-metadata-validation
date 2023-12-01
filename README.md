# TDR Metadata Validation

This validation library is used to validate closure and descriptive metadata as per the provided metadata criteria.
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

## Using updated validation library locally
To use the updated library locally for development, run the following command:

`sbt publishLocal`

This will place a snapshot version of the built project jar in the local .ivy cache folder: $HOME/.ivy2/local/uk.gov.nationalarchives/tdr-metadata-validation_2.13/[version number]-SNAPSHOT

Other sbt projects that have this project as a dependency can access the local snapshot version by changing the version number in their build.sbt file, for example:

```
... other dependencies...
"uk.gov.nationalarchives" %% "tdr-metadata-validation" % "0.0.9-SNAPSHOT"
... other dependences...
```
