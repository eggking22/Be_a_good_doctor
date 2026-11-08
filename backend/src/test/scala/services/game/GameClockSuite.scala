package services.game

import munit.FunSuite
import services.game.calculators.GameClock

final class GameClockSuite extends FunSuite:
  test("首次营业会从 08:30 跳到 09:00") {
    assertEquals(GameClock.openingMinute(GameClock.preparationStartMinute), GameClock.businessStartMinute)
  }

  test("每个 AI 对话回合推进十分钟，且不会超过 17:00") {
    assertEquals(GameClock.advanceDialogueMinute(9 * 60), 9 * 60 + 10)
    assertEquals(GameClock.advanceDialogueMinute(16 * 60 + 55), GameClock.businessEndMinute)
  }

  test("17:00 后标记为营业结束") {
    assert(!GameClock.hasBusinessEnded(16 * 60 + 59))
    assert(GameClock.hasBusinessEnded(GameClock.businessEndMinute))
  }
