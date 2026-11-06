// 患者分布计算器：根据声誉决定患者属性分布，保留可测试的纯函数边界。
package services.patient.calculators

import services.patient.objects.PatientAttributes

object PatientDistributionCalculator:
  // 根据声誉生成一组可直接参与计算的数值属性。
  private[patient] def attributesFor(reputation: Double): PatientAttributes =
    val normalizedReputation = reputation.max(0.0).min(100.0)
    PatientAttributes(
      illnessDifficulty = if normalizedReputation >= 80 then 5 else if normalizedReputation >= 40 then 3 else 1,
      paymentWillingness = if normalizedReputation >= 60 then 4 else if normalizedReputation >= 20 then 3 else 1,
      communicationDifficulty = if normalizedReputation >= 60 then 1 else if normalizedReputation >= 20 then 3 else 5,
      disputeRisk = if normalizedReputation >= 60 then 0.1 else if normalizedReputation >= 20 then 0.5 else 0.9
    )

  private[patient] def clampReputation(value: Double): Double = value.max(0.0).min(100.0)
