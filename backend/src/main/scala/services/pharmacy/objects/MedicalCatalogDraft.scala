// 医疗目录草稿：与 medical_catalog.draft.json 对应，只用于人工确认后的目录导入。
package services.pharmacy.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class MedicalCatalogDraft(
    schemaVersion: String,
    status: String,
    diseases: List[Disease],
    symptoms: List[Symptom],
    medicines: List[CatalogMedicine],
    diseaseSymptoms: List[CatalogDiseaseSymptom],
    diseaseMedicines: List[CatalogDiseaseMedicine]
)

object MedicalCatalogDraft:
  given Codec.AsObject[MedicalCatalogDraft] = deriveCodec
