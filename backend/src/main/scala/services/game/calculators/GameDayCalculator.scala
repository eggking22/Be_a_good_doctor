// 经营日计算器：只处理确定性的日期推进，不负责数据库访问和 HTTP 请求。
package services.game.calculators

import services.game.objects.GameState

import java.time.Instant

object GameDayCalculator:
  /**
    * 将游戏推进一天。
    *
    * 工资、房租、库存消耗等规则应由 economy/pharmacy 等领域计算后再由 game 编排，
    * 因此这里暂时只推进日期并刷新更新时间。
    */
  private[game] def advanceOneDay(state: GameState): GameState =
    state.copy(
      currentDay = state.currentDay + 1,
      currentMinute = GameClock.preparationStartMinute,
      finance = state.finance.copy(cashFlow = None),
      updatedAt = Instant.now().toString
    )
