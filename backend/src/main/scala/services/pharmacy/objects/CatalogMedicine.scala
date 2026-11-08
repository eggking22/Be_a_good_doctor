// 医疗目录草稿中的药物基础项：价格由独立价格草稿提供。
package services.pharmacy.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class CatalogMedicine(
    id: String,
    name: String,
    level: String
)

object CatalogMedicine:
  given Codec.AsObject[CatalogMedicine] = deriveCodec
