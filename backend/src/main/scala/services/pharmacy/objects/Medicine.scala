// 药品领域对象：描述药品目录基础信息；价格由 MedicinePrice 独立维护。
package services.pharmacy.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class Medicine(
    id: String,
    name: String,
    price: MedicinePrice,
    level: MedicineLevel = MedicineLevel.Ordinary
)

object Medicine:
  given Codec.AsObject[Medicine] = deriveCodec
