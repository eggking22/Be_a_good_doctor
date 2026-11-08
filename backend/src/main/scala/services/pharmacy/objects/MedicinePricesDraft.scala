// 药品价格草稿：与 medicine_prices.draft.json 对应，独立于药品名称和类别维护。
package services.pharmacy.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class MedicinePricesDraft(
    schemaVersion: String,
    status: String,
    prices: List[CatalogMedicinePrice]
)

object MedicinePricesDraft:
  given Codec.AsObject[MedicinePricesDraft] = deriveCodec
