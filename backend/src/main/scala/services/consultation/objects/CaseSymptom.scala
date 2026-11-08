// 病例症状对象：保存本次病例的症状严重程度及是否已经向玩家透露。
package services.consultation.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import services.pharmacy.objects.Symptom

final case class CaseSymptom(
    symptom: Symptom,
    severity: Int,
    revealed: Boolean
)

object CaseSymptom:
  given Codec.AsObject[CaseSymptom] = deriveCodec
