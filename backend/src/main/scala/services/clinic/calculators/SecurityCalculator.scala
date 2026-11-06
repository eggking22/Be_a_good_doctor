// 安保能力计算器：按照有效保安的个人能力求和，并限制在 0～100。
package services.clinic.calculators

import services.clinic.objects.{Staff, StaffRole}

object SecurityCalculator:
  private[clinic] def totalSecurityPower(staff: List[Staff]): Int =
    staff
      .filter(employee => employee.active && employee.role == StaffRole.SecurityGuard)
      .map(_.ability.max(0))
      .sum
      .min(100)
