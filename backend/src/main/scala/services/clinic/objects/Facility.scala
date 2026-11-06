// 设施领域对象：描述诊所设施等级、升级成本和容量增益。
package services.clinic.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class Facility(
    id: String,
    name: String,
    level: Int,
    upgradeCost: BigDecimal,
    capacityBonus: Int
)

object Facility:
  given Codec.AsObject[Facility] = deriveCodec
