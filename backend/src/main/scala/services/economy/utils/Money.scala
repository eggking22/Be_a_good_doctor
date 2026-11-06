// 金额工具：统一金额保留两位小数，避免各服务重复处理精度。
package services.economy.utils

object Money:
  private[economy] def round(value: BigDecimal): BigDecimal = value.setScale(2, BigDecimal.RoundingMode.HALF_UP)

  private[economy] def nonNegative(value: BigDecimal): BigDecimal = if value < 0 then BigDecimal(0) else value
