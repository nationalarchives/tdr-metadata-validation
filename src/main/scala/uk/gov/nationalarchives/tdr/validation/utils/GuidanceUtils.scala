package uk.gov.nationalarchives.tdr.validation.utils

import com.fasterxml.jackson.databind.ObjectMapper
import io.circe.{Decoder, DecodingFailure, HCursor}
import io.circe.generic.auto._
import io.circe.generic.semiauto.deriveDecoder
import io.circe.jawn.decode
import uk.gov.nationalarchives.tdr.schemautils.ConfigUtils

import scala.io.Source
import scala.util.Using

object GuidanceUtils {
  private val METADATA_GUIDANCE_LOCATION = ConfigUtils.mapToMetadataEnvironmentFile("/guidance/metadata-template.json")

  implicit val decodeExample: Decoder[String] = new Decoder[String] {
    final def apply(c: HCursor): Decoder.Result[String] = {
      c.value.asString match {
        case Some(s) => Right(s)
        case None    =>
          c.value.asNumber match {
            case Some(n) => Right(n.toString)
            case None    => Left(DecodingFailure("Expected string or number", c.history))
          }
      }
    }
  }

  implicit val decodeGuidanceItem: Decoder[GuidanceItem] = deriveDecoder[GuidanceItem]

  def loadGuidanceFile: Either[io.circe.Error, Seq[GuidanceItem]] = {
    val nodeSchema = getClass.getResourceAsStream(METADATA_GUIDANCE_LOCATION)
    val source = Source.fromInputStream(nodeSchema)
    val jsonString = Using(source)(_.mkString).get
    val mapper = new ObjectMapper()
    val data = mapper.readTree(jsonString).toPrettyString
    decode[List[GuidanceItem]](data)
  }

  case class GuidanceItem(property: String, details: String, format: String, tdrRequirement: String, example: String)
}
