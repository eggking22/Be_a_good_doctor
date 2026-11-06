// 现金流水持久化对象：记录一次收入或支出，不参与 GameState 的当前状态组合。
package services.economy.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class LedgerTransaction(
    id: String,
    gameId: String,
    transactionType: String,
    amount: BigDecimal,
    occurredOn: Int,
    description: String
)

object LedgerTransaction:
  given Codec.AsObject[LedgerTransaction] = deriveCodec
