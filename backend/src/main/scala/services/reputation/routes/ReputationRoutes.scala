// 声誉服务注册：后续注册声誉查询和患者生态分布 API Message。
package services.reputation.routes

import services.system.api.ApiServiceMessages

object ReputationRoutes:
  val apiMessages: ApiServiceMessages = ApiServiceMessages("reputation", Nil)
