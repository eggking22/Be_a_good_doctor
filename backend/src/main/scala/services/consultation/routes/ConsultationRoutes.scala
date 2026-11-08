// 接诊服务注册：后续注册问诊会话、对话和治疗方案评价 API Message。
package services.consultation.routes

import io.circe.generic.auto.*
import services.consultation.api.{EndConsultationAPIMessage, GeneratePatientOpeningDialogueAPIMessage, StartConsultationAPIMessage, SubmitPlayerTextAPIMessage}
import services.consultation.objects.apiTypes.{EndConsultationResponse, PatientDialogueResponse, PlayerTextDialogueResponse, StartConsultationResponse}
import services.system.api.ApiServiceMessages
import services.system.api.RegisteredAPIMessage

object ConsultationRoutes:
  val apiMessages: ApiServiceMessages = ApiServiceMessages(
    "consultation",
    List(
      RegisteredAPIMessage.api[StartConsultationAPIMessage, StartConsultationResponse],
      RegisteredAPIMessage.api[GeneratePatientOpeningDialogueAPIMessage, PatientDialogueResponse],
      RegisteredAPIMessage.api[SubmitPlayerTextAPIMessage, PlayerTextDialogueResponse],
      RegisteredAPIMessage.api[EndConsultationAPIMessage, EndConsultationResponse]
    )
  )
