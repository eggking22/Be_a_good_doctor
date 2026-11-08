// 到诊数量规则：游戏服务决定何时、多少患者到来；患者服务只负责生成单个患者。
package services.game.calculators

object PatientArrivalCalculator:
  private[game] val initialPatientCount = 1

  private[game] def dailyPatientCount(reputation: Double): Int =
    val normalizedReputation = reputation.max(0.0).min(100.0)
    if normalizedReputation >= 80 then 3
    else if normalizedReputation >= 40 then 2
    else 1
