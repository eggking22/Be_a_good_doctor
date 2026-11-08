// 经营时钟：以游戏内分钟保存，不依赖真实世界时间。
package services.game.calculators

object GameClock:
  val preparationStartMinute: Int = 8 * 60 + 30
  val businessStartMinute: Int = 9 * 60
  val businessEndMinute: Int = 17 * 60
  val dialogueRoundMinutes: Int = 10

  def openingMinute(currentMinute: Int): Int =
    currentMinute.max(businessStartMinute)

  def advanceDialogueMinute(currentMinute: Int): Int =
    (currentMinute + dialogueRoundMinutes).min(businessEndMinute)

  def hasBusinessEnded(currentMinute: Int): Boolean =
    currentMinute >= businessEndMinute
