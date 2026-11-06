// 治疗方案评价器：由游戏规则判断方案是否匹配，不能由 LLM 直接修改库存或声誉。
package services.consultation.calculators

import services.consultation.objects.TreatmentEvaluation

object TreatmentEvaluator:
  private[consultation] def evaluate(isMatched: Boolean, communicationScore: Double): TreatmentEvaluation =
    val normalizedScore = communicationScore.max(0.0).min(1.0)
    if isMatched then
      TreatmentEvaluation(
        matched = true,
        reputationDelta = 0.5 + normalizedScore * 0.5,
        reason = "治疗方案与游戏病例匹配。"
      )
    else
      TreatmentEvaluation(
        matched = false,
        reputationDelta = -1.0 + normalizedScore * 0.5,
        reason = "治疗方案与游戏病例不匹配。"
      )
