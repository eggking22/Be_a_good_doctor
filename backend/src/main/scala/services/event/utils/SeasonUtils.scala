// 季节工具：统一四季取值，供事件和患者生成服务复用。
package services.event.utils

object SeasonUtils:
  private val seasons = Vector("spring", "summer", "autumn", "winter")

  private[event] def fromDay(day: Int): String =
    seasons(((day.max(1) - 1) / 30) % seasons.size)
