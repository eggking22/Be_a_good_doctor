// 症状领域对象：保存患者能够描述的游戏内表现。
package services.pharmacy.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class Symptom(
    id: String,
    name: String
)

object Symptom:
  given Codec.AsObject[Symptom] = deriveCodec
