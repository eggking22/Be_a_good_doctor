// 疾病选择器测试：先锁定疾病难度池，再按抽取概率做加权随机选择。
package services.consultation

import munit.FunSuite
import services.consultation.calculators.DiseaseSelector
import services.patient.objects.PatientAttributes
import services.pharmacy.objects.{Disease, DiseaseAttributes}

import scala.util.Random

final class DiseaseSelectorSuite extends FunSuite:
  private val levelOneAttributes = PatientAttributes(1, 3, 3, 0.2)
  private val commonCold = Disease("common-cold", "感冒", DiseaseAttributes(1, 0.8))
  private val allergicRhinitis = Disease("allergic-rhinitis", "过敏性鼻炎", DiseaseAttributes(1, 0.2))
  private val influenza = Disease("influenza", "流感", DiseaseAttributes(3, 1.0))

  test("一级疾病池按抽取概率加权选择") {
    val diseases = List(commonCold, allergicRhinitis, influenza)

    val commonColdResult = DiseaseSelector.select(levelOneAttributes, diseases, fixedRandom(0.79))
    val allergicRhinitisResult = DiseaseSelector.select(levelOneAttributes, diseases, fixedRandom(0.80))

    assertEquals(commonColdResult, Right(commonCold))
    assertEquals(allergicRhinitisResult, Right(allergicRhinitis))
  }

  test("无效的疾病抽取概率会拒绝生成病例") {
    val invalidDisease = Disease("invalid", "无效疾病", DiseaseAttributes(1, 0.0))

    assertEquals(
      DiseaseSelector.select(levelOneAttributes, List(invalidDisease), fixedRandom(0.0)),
      Left("疾病抽取概率必须大于 0 且不大于 1。")
    )
  }

  private def fixedRandom(value: Double): Random = new Random:
    override def nextDouble(): Double = value
