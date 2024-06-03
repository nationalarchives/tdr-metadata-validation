import sbt.*

object Dependencies {
  lazy val commonsLang3 = "org.apache.commons" % "commons-lang3" % "3.14.0"
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.18"
  lazy val ujson = "com.lihaoyi" % "ujson_native0.5_2.13" % "3.3.1"
  lazy val jacksonModule = "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.17.0"
  lazy val metadataSchema = "uk.gov.nationalarchives" % "da-metadata-schema_3" % "0.0.21"
}
