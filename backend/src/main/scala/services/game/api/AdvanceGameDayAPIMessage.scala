// 经营日推进 API：读取状态、执行纯计算、再将新状态持久化。
package services.game.api

import cats.effect.IO
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import services.game.objects.GameState
import services.game.utils.GameStateOperations
import services.system.api.APIMessage

import java.sql.Connection

final case class AdvanceGameDayAPIMessage(gameId: String) extends APIMessage[GameState]:
  override def plan(connection: Connection): IO[GameState] =
    GameStateOperations.advance(connection, gameId)

object AdvanceGameDayAPIMessage:
  // 请求体 JSON：{"gameId":"game-..."}。
  given Codec.AsObject[AdvanceGameDayAPIMessage] = deriveCodec
