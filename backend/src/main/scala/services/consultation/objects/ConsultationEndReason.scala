// 文字接诊结束原因：供前端决定提示文案，不涉及收费、库存扣减或声誉结算。
package services.consultation.objects

import io.circe.{Codec, Decoder, Encoder}

enum ConsultationEndReason:
  case PlayerRequested
  case DoctorEnded
  case PatientLeft
  case RoundLimitReached

object ConsultationEndReason:
  def fromString(value: String): Either[String, ConsultationEndReason] =
    values.find(_.toString.equalsIgnoreCase(value.trim)).toRight(s"未知接诊结束原因：$value")

  given Codec[ConsultationEndReason] = Codec.from(
    Decoder.decodeString.emap(fromString),
    Encoder.encodeString.contramap(_.toString)
  )
