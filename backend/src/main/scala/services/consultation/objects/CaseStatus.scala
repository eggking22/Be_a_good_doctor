// 病例状态：描述一次疾病病例是否仍在当前就诊流程中。
package services.consultation.objects

import io.circe.{Codec, Decoder, Encoder}

enum CaseStatus:
  case Active
  case Completed
  case FollowUpPlanned
  case Abandoned

object CaseStatus:
  def fromString(value: String): Either[String, CaseStatus] =
    values.find(_.toString.equalsIgnoreCase(value.trim)).toRight(s"未知病例状态：$value")

  given Codec[CaseStatus] = Codec.from(
    Decoder.decodeString.emap(fromString),
    Encoder.encodeString.contramap(_.toString)
  )
