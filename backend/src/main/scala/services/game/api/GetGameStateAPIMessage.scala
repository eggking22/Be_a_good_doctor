// 查询游戏状态 API：按游戏 ID 读取当前经营状态。
package services.game.api

import cats.effect.IO
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import services.game.objects.GameState
import services.game.utils.GameStateOperations
import services.system.api.APIMessage

import java.sql.Connection

final case class GetGameStateAPIMessage(gameId: String) extends APIMessage[GameState]:
  override def plan(connection: Connection): IO[GameState] =
    GameStateOperations.get(connection, gameId)

object GetGameStateAPIMessage:
  // 请求体 JSON：{"gameId":"game-..."}。
  given Codec.AsObject[GetGameStateAPIMessage] = deriveCodec
