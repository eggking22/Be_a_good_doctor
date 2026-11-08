// 玩家本轮意图：只记录可用于后续流程分支的有限类型。
package services.consultation.objects

import io.circe.{Codec, Decoder, Encoder}

enum PlayerIntent:
  case AskSymptom
  case DiscussTreatment
  case DiscussPrice
  case EndConsultation
  case Other

object PlayerIntent:
  def fromString(value: String): Either[String, PlayerIntent] =
    values.find(_.toString.equalsIgnoreCase(value.trim)).toRight(s"未知玩家意图：$value")

  given Codec[PlayerIntent] = Codec.from(
    Decoder.decodeString.emap(fromString),
    Encoder.encodeString.contramap(_.toString)
  )
