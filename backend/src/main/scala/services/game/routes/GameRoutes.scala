// 游戏服务注册：后续注册游戏状态和经营日流程 API Message。
package services.game.routes

import io.circe.generic.auto.*
import services.game.api.{AdvanceGameDayAPIMessage, DeleteGameAPIMessage, GetGameStateAPIMessage, StartGameAPIMessage}
import services.game.objects.apiTypes.DeleteGameResponse
import services.system.api.ApiServiceMessages
import services.system.api.RegisteredAPIMessage

object GameRoutes:
  // 只在这里注册游戏服务 API，具体业务编排位于 api 目录。
  val apiMessages: ApiServiceMessages = ApiServiceMessages(
    "game",
    List(
      RegisteredAPIMessage.api[StartGameAPIMessage, services.game.objects.GameState],
      RegisteredAPIMessage.query[GetGameStateAPIMessage, services.game.objects.GameState],
      RegisteredAPIMessage.api[AdvanceGameDayAPIMessage, services.game.objects.GameState],
      RegisteredAPIMessage.api[DeleteGameAPIMessage, DeleteGameResponse]
    )
  )
