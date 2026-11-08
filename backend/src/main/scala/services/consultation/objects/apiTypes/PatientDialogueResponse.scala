// 患者话术响应：只返回患者实际说出的文字和模型名称，不泄露疾病或症状编号。
package services.consultation.objects.apiTypes

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import services.consultation.objects.DialogueMessage

final case class PatientDialogueResponse(
    patientMessage: DialogueMessage,
    modelName: String
)

object PatientDialogueResponse:
  given Codec.AsObject[PatientDialogueResponse] = deriveCodec
