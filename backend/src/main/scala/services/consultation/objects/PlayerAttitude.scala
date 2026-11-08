// 玩家沟通态度：后续声誉结算只允许读取此有限状态。
package services.consultation.objects

import io.circe.{Codec, Decoder, Encoder}

enum PlayerAttitude:
  case Polite
  case Neutral
  case Rude
  case Abusive

object PlayerAttitude:
  def fromString(value: String): Either[String, PlayerAttitude] =
    values.find(_.toString.equalsIgnoreCase(value.trim)).toRight(s"未知玩家态度：$value")

  given Codec[PlayerAttitude] = Codec.from(
    Decoder.decodeString.emap(fromString),
    Encoder.encodeString.contramap(_.toString)
  )
