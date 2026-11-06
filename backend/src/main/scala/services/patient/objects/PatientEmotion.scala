// 患者情绪枚举：避免用任意字符串表示固定状态。
package services.patient.objects

import io.circe.{Codec, Decoder, Encoder}

enum PatientEmotion:
  case Calm
  case Angry
  case Anxious
  case Happy

object PatientEmotion:
  def fromString(value: String): Either[String, PatientEmotion] =
    values.find(_.toString.equalsIgnoreCase(value.trim)).toRight(s"未知患者情绪：$value")

  // 使用枚举名称作为 JSON 字符串，便于前后端传输和数据库映射。
  given Codec[PatientEmotion] = Codec.from(
    Decoder.decodeString.emap(fromString),
    Encoder.encodeString.contramap(_.toString)
  )
