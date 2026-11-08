// 药房总览：同时返回当前库存、市场药品与可采购时间状态。
package services.pharmacy.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class PharmacyOverview(
    gameId: String,
    cash: BigDecimal,
    currentMinute: Int,
    canPurchase: Boolean,
    inventory: Inventory,
    marketMedicines: List[Medicine]
)

object PharmacyOverview:
  given Codec.AsObject[PharmacyOverview] = deriveCodec
