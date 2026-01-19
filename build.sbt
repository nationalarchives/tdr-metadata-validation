import Dependencies.*
import sbt.url
import sbtrelease.ReleaseStateTransformations.*

ThisBuild / organization := "uk.gov.nationalarchives"
ThisBuild / organizationName := "National Archives"

scalaVersion := "2.13.18"
version := version.value

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/nationalarchives/tdr-metadata-validation"),
    "git@github.com:nationalarchives/tdr-metadata-validation.git"
  )
)

developers := List(
  Developer(
    id = "tna-da-bot",
    name = "TNA Digital Archiving",
    email = "s-GitHubDABot@nationalarchives.gov.uk",
    url = url("https://github.com/nationalarchives/tdr-metadata-validation")
  )
)

ThisBuild / description := "A library to validate input metadata for Transfer Digital Records"
ThisBuild / licenses := List("MIT" -> new URL("https://choosealicense.com/licenses/mit/"))
ThisBuild / homepage := Some(url("https://github.com/nationalarchives/tdr-metadata-validation"))

useGpgPinentry := true
publishTo := {
  val centralSnapshots = "https://central.sonatype.com/repository/maven-snapshots/"
  if (isSnapshot.value) Some("central-snapshots" at centralSnapshots)
  else localStaging.value
}
publishMavenStyle := true

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommand("publishSigned"),
  releaseStepCommand("sonaRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)

lazy val root = (project in file("."))
  .settings(
    name := "tdr-metadata-validation",
    libraryDependencies ++= Seq(
      commonsLang3,
      ujson,
      jsonSchemaValidator,
      pekkoTestKit % Test,
      catsEffect,
      nscalaTime,
      jacksonModule,
      metadataSchema,
      scalaTest % Test
    )
  )
