// 医疗目录草稿中的疾病—症状关系。
package services.pharmacy.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class CatalogDiseaseSymptom(
    diseaseId: String,
    symptomId: String
)

object CatalogDiseaseSymptom:
  given Codec.AsObject[CatalogDiseaseSymptom] = deriveCodec
