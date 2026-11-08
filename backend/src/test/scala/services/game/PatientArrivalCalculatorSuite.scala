package services.game

import munit.FunSuite
import services.game.calculators.PatientArrivalCalculator

final class PatientArrivalCalculatorSuite extends FunSuite:
  test("开局固定生成一名患者") {
    assertEquals(PatientArrivalCalculator.initialPatientCount, 1)
  }

  test("每日到诊人数按声誉分为一到三名") {
    assertEquals(PatientArrivalCalculator.dailyPatientCount(10), 1)
    assertEquals(PatientArrivalCalculator.dailyPatientCount(60), 2)
    assertEquals(PatientArrivalCalculator.dailyPatientCount(90), 3)
  }
