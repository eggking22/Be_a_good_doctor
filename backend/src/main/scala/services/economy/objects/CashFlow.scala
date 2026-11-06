// 现金流领域对象：区分收入、支出和期末现金，避免把库存资产直接当作销售成本。
package services.economy.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class CashFlow(
    income: BigDecimal,
    expense: BigDecimal
)

object CashFlow:
  given Codec.AsObject[CashFlow] = deriveCodec
