package services.patient

import munit.FunSuite
import services.patient.calculators.PatientGenerator
import services.patient.objects.{PatientEmotion}

import scala.util.Random

final class PatientGeneratorSuite extends FunSuite:
  test("患者生成器每次生成一名与声誉对应属性的患者") {
    val patient = PatientGenerator.generate(60.0, new Random(7))

    assert(patient.id.startsWith("patient-"))
    assert(patient.name.nonEmpty)
    assertEquals(patient.attributes.illnessDifficulty, 3)
    assertEquals(patient.attributes.paymentWillingness, 4)
    assertEquals(patient.attributes.communicationDifficulty, 1)
    assertEquals(patient.attributes.disputeRisk, 0.1)
    assert(PatientEmotion.values.contains(patient.initialEmotion))
  }
