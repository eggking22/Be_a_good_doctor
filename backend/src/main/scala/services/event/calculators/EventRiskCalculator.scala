// 事件风险计算器：综合声誉、安保和随机因子，输出事件风险分数。
package services.event.calculators

object EventRiskCalculator:
  private[event] def score(reputation: Double, securityPower: Int, randomFactor: Double): Double =
    val reputationRisk = (100.0 - reputation.max(0.0).min(100.0)) / 100.0
    val securityReduction = securityPower.max(0).min(100).toDouble / 100.0
    (reputationRisk * 0.6 + randomFactor.max(0).min(1) * 0.4) * (1 - securityReduction * 0.5)
