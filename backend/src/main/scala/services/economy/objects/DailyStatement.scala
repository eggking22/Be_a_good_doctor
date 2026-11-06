// 日结算对象：保存一天经营结束后的现金流和破产结果。
package services.economy.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class DailyStatement(
    openingCash: BigDecimal,
    income: BigDecimal,
    expense: BigDecimal,
    closingCash: BigDecimal,
    bankrupt: Boolean
)

object DailyStatement:
  given Codec.AsObject[DailyStatement] = deriveCodec
