// 医疗目录草稿中的疾病—药物关系。
package services.pharmacy.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class CatalogDiseaseMedicine(
    diseaseId: String,
    medicineId: String
)

object CatalogDiseaseMedicine:
  given Codec.AsObject[CatalogDiseaseMedicine] = deriveCodec
