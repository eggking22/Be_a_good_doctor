// 财务状态聚合：保存当前现金以及最近一次结算产生的财务记录。
package services.economy.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class FinanceState(
    cash: BigDecimal,
    cashFlow: Option[CashFlow],
    dailyStatement: Option[DailyStatement]
)

object FinanceState:
  given Codec.AsObject[FinanceState] = deriveCodec
