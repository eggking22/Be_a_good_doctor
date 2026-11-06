// 事件服务注册：后续注册事件生成、事件查询和事件处理 API Message。
package services.event.routes

import services.system.api.ApiServiceMessages

object EventRoutes:
  val apiMessages: ApiServiceMessages = ApiServiceMessages("event", Nil)
