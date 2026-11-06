// 删除游戏 API：删除游戏主记录，由数据库外键级联清理关联数据。
package services.game.api

import cats.effect.IO
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import services.game.utils.GameStateOperations
import services.game.objects.apiTypes.DeleteGameResponse
import services.system.api.APIMessage

import java.sql.Connection

final case class DeleteGameAPIMessage(gameId: String) extends APIMessage[DeleteGameResponse]:
  override def plan(connection: Connection): IO[DeleteGameResponse] =
    GameStateOperations.delete(connection, gameId).map(_ => DeleteGameResponse(gameId.trim, deleted = true))

object DeleteGameAPIMessage:
  given Codec.AsObject[DeleteGameAPIMessage] = deriveCodec
