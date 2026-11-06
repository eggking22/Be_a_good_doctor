// 事件类型枚举：限定随机事件所属的业务类别。
package services.event.objects

import io.circe.{Codec, Decoder, Encoder}

enum EventType:
  case MedicalDispute
  case SecurityIncident
  case EmployeeIncident
  case SeasonalEvent
  case MarketEvent

object EventType:
  def fromString(value: String): Either[String, EventType] =
    values.find(_.toString.equalsIgnoreCase(value.trim)).toRight(s"未知事件类型：$value")

  given Codec[EventType] = Codec.from(
    Decoder.decodeString.emap(fromString),
    Encoder.encodeString.contramap(_.toString)
  )
