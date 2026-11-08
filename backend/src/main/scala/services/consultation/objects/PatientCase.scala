// 病例对象：保存患者本次就诊对应的隐藏疾病和症状集合。
package services.consultation.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import services.pharmacy.objects.Disease

final case class PatientCase(
    id: String,
    patientId: String,
    disease: Disease,
    symptoms: List[CaseSymptom],
    status: CaseStatus
)

object PatientCase:
  given Codec.AsObject[PatientCase] = deriveCodec
