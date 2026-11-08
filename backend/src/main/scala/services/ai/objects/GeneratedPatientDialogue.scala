// 模型生成的单轮患者话术及可验证的玩家文字理解结果。
package services.ai.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import services.consultation.objects.{PatientConversationAction, PlayerTextAnalysis}

final case class GeneratedPatientDialogue(
    text: String,
    mentionedSymptomIds: List[String],
    playerTextAnalysis: Option[PlayerTextAnalysis],
    patientConversationAction: PatientConversationAction,
    shouldLeave: Boolean,
    modelName: String
)

object GeneratedPatientDialogue:
  given Codec.AsObject[GeneratedPatientDialogue] = deriveCodec
