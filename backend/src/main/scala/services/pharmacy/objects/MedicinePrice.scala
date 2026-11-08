// 药品价格对象：将易调整的采购价和售价独立出来，避免价格字段堆积在药品基础对象中。
package services.pharmacy.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class MedicinePrice(
    purchasePrice: BigDecimal,
    salePrice: BigDecimal
)

object MedicinePrice:
  given Codec.AsObject[MedicinePrice] = deriveCodec
