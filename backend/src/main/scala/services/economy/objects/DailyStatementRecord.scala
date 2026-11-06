// 日结算持久化对象：为 DailyStatement 增加游戏、经营日和数据库记录 ID。
package services.economy.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class DailyStatementRecord(
    id: String,
    gameId: String,
    day: Int,
    statement: DailyStatement,
    createdAt: String
)

object DailyStatementRecord:
  given Codec.AsObject[DailyStatementRecord] = deriveCodec
