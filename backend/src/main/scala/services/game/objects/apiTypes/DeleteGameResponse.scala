// 删除游戏 API 的传输结果，不属于 GameState 核心领域对象。
package services.game.objects.apiTypes

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class DeleteGameResponse(gameId: String, deleted: Boolean)

object DeleteGameResponse:
  given Codec.AsObject[DeleteGameResponse] = deriveCodec
