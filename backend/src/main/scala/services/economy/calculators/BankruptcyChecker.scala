// 破产判断器：集中管理现金链断裂的判定规则。
package services.economy.calculators

object BankruptcyChecker:
  private[economy] def isBankrupt(cash: BigDecimal, requiredPayment: BigDecimal = BigDecimal(0)): Boolean =
    cash - requiredPayment < 0
