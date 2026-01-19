import sbt.*

object Dependencies {
  private val pekkoVersion = "1.2.1"

  lazy val commonsLang3 = "org.apache.commons" % "commons-lang3" % "3.20.0"
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.19"
  lazy val ujson = "com.lihaoyi" % "ujson_native0.5_2.13" % "4.4.2"
  lazy val jacksonModule = "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.20.1"
  lazy val metadataSchema = "uk.gov.nationalarchives" %% "da-metadata-schema" % "0.0.99"
  lazy val jsonSchemaValidator = "com.networknt" % "json-schema-validator" % "3.0.0"
  lazy val pekkoActor = "org.apache.pekko" %% "pekko-actor-typed" % pekkoVersion
  lazy val pekkoConnectors = "org.apache.pekko" %% "pekko-connectors-csv" % pekkoVersion
  lazy val pekkoStream = "org.apache.pekko" %% "pekko-stream" % pekkoVersion
  lazy val pekkoTestKit = "org.apache.pekko" %% "pekko-testkit" % pekkoVersion
  lazy val catsEffect = "org.typelevel" %% "cats-effect" % "3.6.3"
  lazy val nscalaTime = "com.github.nscala-time" %% "nscala-time" % "3.0.0"
  lazy val circeCore = "io.circe" %% "circe-core" % "0.14.15"
  val circeGeneric = "io.circe" %% "circe-generic" % "0.14.15"
  val circeParser = "io.circe" %% "circe-parser" % "0.14.15"

}
