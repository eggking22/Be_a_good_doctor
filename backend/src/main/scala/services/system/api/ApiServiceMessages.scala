// 服务 API 注册结构：保存某个服务当前暴露的 API Message 列表。
package services.system.api

final case class ApiServiceMessages(
    serviceName: String,
    messages: List[RegisteredAPIMessage]
)
