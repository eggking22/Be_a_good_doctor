// 开始游戏 API：创建一局新的诊所经营游戏并写入初始状态。
package services.game.api

import cats.effect.IO
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import services.game.objects.GameState
import services.game.utils.GameStateOperations
import services.system.api.APIMessage

import java.sql.Connection

final case class StartGameAPIMessage(playerName: String, replaceGameId: Option[String] = None) extends APIMessage[GameState]:
  override def plan(connection: Connection): IO[GameState] =
    GameStateOperations.start(connection, playerName, replaceGameId)

object StartGameAPIMessage:
  // 请求体 JSON：{"playerName":"玩家","replaceGameId":"旧存档 ID（可选）"}。
  given Codec.AsObject[StartGameAPIMessage] = deriveCodec
