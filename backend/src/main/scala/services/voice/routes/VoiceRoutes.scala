// 语音能力适配层：未来如需 WebSocket/流式接口，再在此注册专用路由。
package services.voice.routes

import services.system.api.ApiServiceMessages

object VoiceRoutes:
  val apiMessages: ApiServiceMessages = ApiServiceMessages("voice", Nil)
