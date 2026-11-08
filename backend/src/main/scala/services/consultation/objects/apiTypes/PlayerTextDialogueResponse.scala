// 玩家文字接诊响应：返回已保存的两条消息及受限理解结果。
package services.consultation.objects.apiTypes

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import services.consultation.objects.{ConsultationEndReason, ConsultationStatus, DialogueMessage, PlayerTextAnalysis, PrescriptionItem}

final case class PlayerTextDialogueResponse(
    playerMessage: DialogueMessage,
    patientMessage: Option[DialogueMessage],
    analysis: PlayerTextAnalysis,
    shoppingList: List[PrescriptionItem],
    status: ConsultationStatus,
    endReason: Option[ConsultationEndReason],
    modelName: String
)

object PlayerTextDialogueResponse:
  given Codec.AsObject[PlayerTextDialogueResponse] = deriveCodec
