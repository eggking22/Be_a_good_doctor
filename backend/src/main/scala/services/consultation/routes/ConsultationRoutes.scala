// 接诊服务注册：后续注册问诊会话、对话和治疗方案评价 API Message。
package services.consultation.routes

import services.system.api.ApiServiceMessages

object ConsultationRoutes:
  val apiMessages: ApiServiceMessages = ApiServiceMessages("consultation", Nil)
