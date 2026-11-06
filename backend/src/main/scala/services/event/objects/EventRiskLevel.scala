// 事件风险等级枚举：统一事件风险展示和处理条件。
package services.event.objects

import io.circe.{Codec, Decoder, Encoder}

enum EventRiskLevel:
  case Low
  case Medium
  case High

object EventRiskLevel:
  def fromString(value: String): Either[String, EventRiskLevel] =
    values.find(_.toString.equalsIgnoreCase(value.trim)).toRight(s"未知事件风险等级：$value")

  given Codec[EventRiskLevel] = Codec.from(
    Decoder.decodeString.emap(fromString),
    Encoder.encodeString.contramap(_.toString)
  )
