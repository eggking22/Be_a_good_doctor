// 医疗目录价格草稿中的单项价格；金额先以字符串读取，再在导入时转换为 BigDecimal。
package services.pharmacy.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class CatalogMedicinePrice(
    medicineId: String,
    purchasePrice: String,
    salePrice: String
)

object CatalogMedicinePrice:
  given Codec.AsObject[CatalogMedicinePrice] = deriveCodec
