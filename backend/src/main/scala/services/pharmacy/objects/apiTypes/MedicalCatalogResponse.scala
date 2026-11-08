// 面向前端的医疗目录读取结果：返回目录基础对象和两类多对多关系。
package services.pharmacy.objects.apiTypes

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import services.pharmacy.objects.{CatalogDiseaseMedicine, CatalogDiseaseSymptom, Disease, Medicine, Symptom}

final case class MedicalCatalogResponse(
    diseases: List[Disease],
    symptoms: List[Symptom],
    medicines: List[Medicine],
    diseaseSymptoms: List[CatalogDiseaseSymptom],
    diseaseMedicines: List[CatalogDiseaseMedicine]
)

object MedicalCatalogResponse:
  given Codec.AsObject[MedicalCatalogResponse] = deriveCodec
