// 患者话术上下文：仅传递公开对话所需的症状、性格和情绪，不传递隐藏疾病。
package services.ai.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import services.consultation.objects.DialogueMessage
import services.patient.objects.PatientEmotion

final case class PatientDialogueContext(
    patientName: String,
    communicationDifficulty: Int,
    emotion: PatientEmotion,
    symptomsAllowed: List[DialogueSymptom],
    medicinesAllowed: List[DialogueMedicine],
    recentMessages: List[DialogueMessage],
    playerText: Option[String],
    hasExistingPrescription: Boolean = false
)

object PatientDialogueContext:
  given Codec.AsObject[PatientDialogueContext] = deriveCodec
