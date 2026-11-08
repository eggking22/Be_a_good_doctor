// 疾病选择器：根据患者疾病难度从共享疾病目录中选择隐藏疾病。
package services.consultation.calculators

import services.patient.objects.PatientAttributes
import services.pharmacy.objects.Disease

import scala.util.Random

object DiseaseSelector:
  private[consultation] def select(
      attributes: PatientAttributes,
      diseases: List[Disease],
      random: Random
  ): Either[String, Disease] =
    if diseases.isEmpty then Left("疾病目录为空，无法生成病例。")
    else
      val targetDifficulty = attributes.illnessDifficulty.max(1).min(5)
      val exactCandidates = diseases.filter(_.attributes.difficulty == targetDifficulty)
      val candidates =
        if exactCandidates.nonEmpty then exactCandidates
        else
          val nearestDifficulty = diseases.map(_.attributes.difficulty).minBy(difficulty => math.abs(difficulty - targetDifficulty))
          diseases.filter(_.attributes.difficulty == nearestDifficulty)
      selectByProbability(candidates, random)

  private def selectByProbability(candidates: List[Disease], random: Random): Either[String, Disease] =
    val probabilities = candidates.map(_.attributes.extractionProbability)
    if probabilities.exists(probability => probability <= 0 || probability > 1 || probability.isNaN || probability.isInfinity) then
      Left("疾病抽取概率必须大于 0 且不大于 1。")
    else
      val totalProbability = probabilities.sum
      val target = random.nextDouble() * totalProbability
      val selected = candidates.zip(probabilities).foldLeft((0.0, Option.empty[Disease])) {
        case ((accumulated, Some(disease)), _) => (accumulated, Some(disease))
        case ((accumulated, None), (disease, probability)) =>
          val next = accumulated + probability
          if target < next then (next, Some(disease)) else (next, None)
      }._2.getOrElse(candidates.last)
      Right(selected)
