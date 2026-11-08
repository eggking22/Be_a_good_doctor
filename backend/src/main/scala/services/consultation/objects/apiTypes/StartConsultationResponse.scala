// 开始接诊响应：只返回接诊公开信息，不暴露病例的隐藏疾病和完整症状。
package services.consultation.objects.apiTypes

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import services.consultation.objects.ConsultationStatus
import services.patient.objects.{Patient, PatientEmotion}

final case class StartConsultationResponse(
    consultationId: String,
    patient: Patient,
    emotion: PatientEmotion,
    status: ConsultationStatus,
    opening: PatientDialogueResponse
)

object StartConsultationResponse:
  given Codec.AsObject[StartConsultationResponse] = deriveCodec
