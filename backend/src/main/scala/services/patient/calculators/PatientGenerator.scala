// 患者生成器：每次调用只构造一名患者，不决定队列人数或持久化方式。
package services.patient.calculators

import services.patient.objects.{Patient, PatientEmotion}
import services.patient.utils.PatientIdGenerator

import scala.util.Random

object PatientGenerator:
  private val surnames = Vector("王", "李", "张", "刘", "陈", "杨", "黄", "赵", "周", "吴", "徐", "孙")
  private val givenNames = Vector("子安", "雨桐", "明远", "若溪", "思源", "嘉宁", "浩然", "书涵", "安然", "景行", "念慈", "知夏")
  private val emotions = PatientEmotion.values.toVector

  private[patient] def generate(reputation: Double, random: Random): Patient =
    val attributes = PatientDistributionCalculator.attributesFor(reputation)
    val name = pick(surnames, random) + pick(givenNames, random)
    val initialEmotion = pick(emotions, random)
    Patient(
      id = PatientIdGenerator.nextId(),
      name = name,
      attributes = attributes,
      initialEmotion = initialEmotion
    )

  private[patient] def generate(reputation: Double): Patient =
    generate(reputation, new Random())

  private def pick[A](options: IndexedSeq[A], random: Random): A =
    options(random.nextInt(options.size))
