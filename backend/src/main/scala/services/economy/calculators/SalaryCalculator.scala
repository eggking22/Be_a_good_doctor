// 工资计算器：按照员工日工资求和，规则不依赖 HTTP、数据库或前端。
package services.economy.calculators

object SalaryCalculator:
  private[economy] def totalDailySalary(dailySalaries: List[BigDecimal]): BigDecimal =
    dailySalaries.filter(_ >= 0).sum
