// 现金流计算器：只计算收入、支出和期末现金，不访问数据库。
package services.economy.calculators

import services.economy.objects.{CashFlow, DailyStatement}

object CashFlowCalculator:
  def settle(openingCash: BigDecimal, cashFlow: CashFlow): DailyStatement =
    // 净现金流属于经济计算，不放在 CashFlow 数据对象中。
    val netCashFlow = cashFlow.income - cashFlow.expense
    val closingCash = openingCash + netCashFlow
    DailyStatement(
      openingCash = openingCash,
      income = cashFlow.income,
      expense = cashFlow.expense,
      closingCash = closingCash,
      bankrupt = closingCash < 0
    )
