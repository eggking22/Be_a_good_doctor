// 保存期限工具：封装日期字符串规范校验，避免库存规则散落在 API 层。
package services.pharmacy.utils

import java.time.LocalDate

object ExpiryUtils:
  private[pharmacy] def isIsoDate(value: String): Boolean =
    try
      LocalDate.parse(value)
      true
    catch
      case _: java.time.format.DateTimeParseException => false
