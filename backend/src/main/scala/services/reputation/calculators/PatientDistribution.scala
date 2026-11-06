// 患者生态计算器：将声誉区间映射为患者群体标签。
package services.reputation.calculators

object PatientDistribution:
  private[reputation] def clinicType(reputation: Double): String =
    reputation match
      case score if score >= 80 => "known-clinic"
      case score if score >= 60 => "community-clinic"
      case score if score >= 40 => "ordinary-clinic"
      case score if score >= 20 => "low-reputation-clinic"
      case _ => "chaotic-clinic"
