// 完整读取存档 API：主菜单载入后可恢复经营状态及接诊过程数据。
package services.game.api

import cats.effect.IO
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import services.game.objects.LoadedGameState
import services.game.utils.GameStateOperations
import services.system.api.APIMessage

import java.sql.Connection

final case class LoadGameAPIMessage(gameId: String) extends APIMessage[LoadedGameState]:
  override def plan(connection: Connection): IO[LoadedGameState] =
    GameStateOperations.loadComplete(connection, gameId)

object LoadGameAPIMessage:
  given Codec.AsObject[LoadGameAPIMessage] = deriveCodec
