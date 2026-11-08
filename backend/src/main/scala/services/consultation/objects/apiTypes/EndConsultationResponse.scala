// 结束文字接诊的返回结果：保留购物清单，结算仍由后续阶段独立处理。
package services.consultation.objects.apiTypes

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import services.consultation.objects.{ConsultationEndReason, ConsultationStatus, DialogueMessage, PlayerTextAnalysis, PrescriptionItem}

final case class EndConsultationResponse(
    playerMessage: DialogueMessage,
    patientMessage: DialogueMessage,
    analysis: Option[PlayerTextAnalysis],
    status: ConsultationStatus,
    endReason: ConsultationEndReason,
    shoppingList: List[PrescriptionItem],
    modelName: String
)

object EndConsultationResponse:
  given Codec.AsObject[EndConsultationResponse] = deriveCodec
