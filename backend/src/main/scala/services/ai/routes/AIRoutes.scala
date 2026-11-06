// AI 能力适配层：默认不直接向前端暴露业务 API，由 consultation 调用。
package services.ai.routes

import services.system.api.ApiServiceMessages

object AIRoutes:
  val apiMessages: ApiServiceMessages = ApiServiceMessages("ai", Nil)
