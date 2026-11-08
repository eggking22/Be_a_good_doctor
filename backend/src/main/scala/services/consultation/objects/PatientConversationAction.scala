// 患者在本轮药品被识别后选择的对话走向；由 AI 受限返回、后端校验后决定是否结束文字问诊。
package services.consultation.objects

import io.circe.{Codec, Decoder, Encoder}

enum PatientConversationAction:
  case Continue
  case AskFollowUp
  case Leave

object PatientConversationAction:
  def fromString(value: String): Either[String, PatientConversationAction] =
    values.find(_.toString.equalsIgnoreCase(value.trim)).toRight(s"未知患者对话动作：$value")

  given Codec[PatientConversationAction] = Codec.from(
    Decoder.decodeString.emap(fromString),
    Encoder.encodeString.contramap(_.toString)
  )
