// 药品领域对象：描述药品价格、库存和保存期限等经营属性。
package services.pharmacy.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class Medicine(
    id: String,
    name: String,
    purchasePrice: BigDecimal,
    salePrice: BigDecimal
)

object Medicine:
  given Codec.AsObject[Medicine] = deriveCodec
