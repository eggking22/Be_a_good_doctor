// 事件领域对象：事件本身描述影响和状态，实际影响由对应领域规则执行。
package services.event.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class GameEvent(
    id: String,
    eventType: EventType,
    title: String,
    description: String,
    riskLevel: EventRiskLevel,
    resolved: Boolean
)

object GameEvent:
  given Codec.AsObject[GameEvent] = deriveCodec
