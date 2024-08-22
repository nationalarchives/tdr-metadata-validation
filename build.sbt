import Dependencies.*
import sbt.url
import sbtrelease.ReleaseStateTransformations.*

lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    jacksonModule,
    metadataSchema,
    scalaTest % Test
  ),

  ThisBuild / organization := "uk.gov.nationalarchives",
  ThisBuild / organizationName := "National Archives",

  scalaVersion := "2.13.14",
  version := version.value,

  ThisBuild / scmInfo := Some(
    ScmInfo(
      url("https://github.com/nationalarchives/tdr-metadata-validation"),
      "git@github.com:nationalarchives/tdr-metadata-validation.git"
    )
  ),

  developers := List(
    Developer(
      id = "tna-digital-archiving-jenkins",
      name = "TNA Digital Archiving",
      email = "digitalpreservation@nationalarchives.gov.uk",
      url = url("https://github.com/nationalarchives/tdr-metadata-validation")
    )
  ),

  ThisBuild / description := "A library to validate input metadata for Transfer Digital Records",
  ThisBuild / licenses := List("MIT" -> new URL("https://choosealicense.com/licenses/mit/")),
  ThisBuild / homepage := Some(url("https://github.com/nationalarchives/tdr-metadata-validation")),

  useGpgPinentry := true,
  publishTo := sonatypePublishToBundle.value,
  publishMavenStyle := true,

  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommand("publishSigned"),
    releaseStepCommand("sonatypeBundleRelease"),
    setNextVersion,
    commitNextVersion,
    pushChanges
  ),

  resolvers +=
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
)

lazy val validation = (project in file("validation"))
  .settings(commonSettings).settings(
    name := "tdr-metadata-validation",
    description := "A project for validating metadata",
    libraryDependencies ++= Seq(
      commonsLang3,
      ujson,
      jsonSchemaValidator,
      pekkoTestKit % Test,
      catsEffect,
      nscalaTime
    )
  )

lazy val schemaUtils = (project in file("schema-utils"))
  .settings(commonSettings).settings(
    name := "tdr-schema-utils",
    description := "A project containing utils methods for TNA schema",
    libraryDependencies ++= Seq(
      //specific dependencies for project
    )
  )


lazy val root = (project in file("."))
  .settings(commonSettings)
  .settings(
    name := "tdr-metadata-validation",
    publish / skip := true
  ).aggregate(validation, schemaUtils)
