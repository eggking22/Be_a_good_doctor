// 接诊状态枚举：描述一次接诊当前处于哪个流程阶段。
package services.consultation.objects

import io.circe.{Codec, Decoder, Encoder}

enum ConsultationStatus:
  case Waiting
  case InProgress
  case Completed
  case Referred
  case Abandoned

object ConsultationStatus:
  def fromString(value: String): Either[String, ConsultationStatus] =
    values.find(_.toString.equalsIgnoreCase(value.trim)).toRight(s"未知接诊状态：$value")

  given Codec[ConsultationStatus] = Codec.from(
    Decoder.decodeString.emap(fromString),
    Encoder.encodeString.contramap(_.toString)
  )
