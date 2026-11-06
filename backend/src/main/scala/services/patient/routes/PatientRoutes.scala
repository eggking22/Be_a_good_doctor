// 患者服务注册：后续注册患者生成、队列和患者状态 API Message。
package services.patient.routes

import services.system.api.ApiServiceMessages

object PatientRoutes:
  val apiMessages: ApiServiceMessages = ApiServiceMessages("patient", Nil)
