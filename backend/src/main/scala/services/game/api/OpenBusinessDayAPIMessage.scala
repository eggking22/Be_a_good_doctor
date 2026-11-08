// 开始营业 API：首次接诊时从准备时间跳转到 09:00。
package services.game.api

import cats.effect.IO
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import services.game.objects.GameState
import services.game.utils.GameStateOperations
import services.system.api.APIMessage

import java.sql.Connection

final case class OpenBusinessDayAPIMessage(gameId: String) extends APIMessage[GameState]:
  override def plan(connection: Connection): IO[GameState] =
    GameStateOperations.openBusinessDay(connection, gameId)

object OpenBusinessDayAPIMessage:
  given Codec.AsObject[OpenBusinessDayAPIMessage] = deriveCodec
