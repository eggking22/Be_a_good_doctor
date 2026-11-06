// 声誉评价器：按照病例处理、沟通和违规行为计算变化量。
package services.reputation.calculators

object ReputationEvaluator:
  private[reputation] def delta(caseHandledWell: Boolean, communicationExcellent: Boolean, seriousViolation: Boolean): Double =
    val handlingDelta = if caseHandledWell then 0.5 else -1.0
    val communicationDelta = if communicationExcellent then 0.5 else 0.0
    val violationDelta = if seriousViolation then -5.0 else 0.0
    handlingDelta + communicationDelta + violationDelta

  private[reputation] def applyDelta(currentScore: Double, delta: Double): Double =
    (currentScore + delta).max(0.0).min(100.0)
