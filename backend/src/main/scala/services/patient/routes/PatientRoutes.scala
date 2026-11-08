// 患者服务注册：患者生成和后续队列相关 API Message。
package services.patient.routes

import io.circe.generic.auto.*
import services.patient.api.GeneratePatientAPIMessage
import services.patient.objects.Patient
import services.system.api.ApiServiceMessages
import services.system.api.RegisteredAPIMessage

object PatientRoutes:
  val apiMessages: ApiServiceMessages = ApiServiceMessages(
    "patient",
    List(
      RegisteredAPIMessage.api[GeneratePatientAPIMessage, Patient]
    )
  )
