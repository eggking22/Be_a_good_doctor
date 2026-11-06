// 员工满意度计算器：把员工个体满意度汇总为诊所经营指标。
package services.clinic.calculators

import services.clinic.objects.Staff

object StaffSatisfactionCalculator:
  private[clinic] def average(staff: List[Staff]): Double =
    if staff.isEmpty then 0.0 else staff.map(_.satisfaction.max(0.0).min(100.0)).sum / staff.size
