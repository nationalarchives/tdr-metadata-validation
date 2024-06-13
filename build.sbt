import Dependencies.*
import sbt.url
import sbtrelease.*
import sbtrelease.ReleaseStateTransformations.*


ThisBuild / organization := "uk.gov.nationalarchives"
ThisBuild / organizationName := "National Archives"

scalaVersion := "2.13.14"
version := version.value

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/nationalarchives/tdr-metadata-validation"),
    "git@github.com:nationalarchives/tdr-metadata-validation.git"
  )
)

developers := List(
  Developer(
    id = "tna-digital-archiving-jenkins",
    name = "TNA Digital Archiving",
    email = "digitalpreservation@nationalarchives.gov.uk",
    url = url("https://github.com/nationalarchives/tdr-metadata-validation")
  )
)

ThisBuild / description := "A library to validate input metadata for Transfer Digital Records"
ThisBuild / licenses := List("MIT" -> new URL("https://choosealicense.com/licenses/mit/"))
ThisBuild / homepage := Some(url("https://github.com/nationalarchives/tdr-metadata-validation"))

useGpgPinentry := true
publishTo := sonatypePublishToBundle.value
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
  releaseStepCommand("sonatypeBundleRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)

enablePlugins(JavaAppPackaging)



topLevelDirectory := Some("java")


// removes all jar mappings in universal and appends the fat jar
//mappings in Universal := {
//  // universalMappings: Seq[(File,String)]
//  val universalMappings = (mappings in Universal).value
//  val fatJar = (assembly in Compile).value
//  // removing means filtering
//  val filtered = universalMappings filter {
//    case (file, name) =>  ! name.endsWith(".jar")
//  }
//  // add the fat jar
//  filtered :+ (fatJar -> ("lib/" + fatJar.getName))
//}


// the bash scripts classpath only needs the fat jar
//scriptClasspath := Seq( (jarName in assembly).value )

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

lazy val root = (project in file("."))
  .settings(
    name := "tdr-metadata-validation",
    libraryDependencies ++= Seq(
      commonsLang3,
      scalaTest % Test,
      ujson,
      jsonSchemaValidator,
      jacksonModule,
      metadataSchema,
      pekkoTestKit % Test,
      catsEffect,
      nscalaTime
    )
  )
