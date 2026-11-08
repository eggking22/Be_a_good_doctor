// 存档列表读取 API：当前没有账户体系，因此返回本机数据库中的全部游戏存档。
package services.game.api

import cats.effect.IO
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import services.game.objects.GameSaveSummary
import services.game.utils.GameStateOperations
import services.system.api.APIMessage

import java.sql.Connection

final case class ListGameSavesAPIMessage() extends APIMessage[List[GameSaveSummary]]:
  override def plan(connection: Connection): IO[List[GameSaveSummary]] =
    GameStateOperations.list(connection)

object ListGameSavesAPIMessage:
  given Codec.AsObject[ListGameSavesAPIMessage] = deriveCodec
