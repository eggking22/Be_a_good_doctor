// 患者属性对象：集中保存疾病难度、付费意愿、沟通难度和医闹概率。
package services.patient.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class PatientAttributes(
    // 疾病难度：1～5，数值越大越难处理。
    illnessDifficulty: Int,
    // 付费意愿：1～5，数值越大越愿意支付。
    paymentWillingness: Int,
    // 沟通难度：1～5，数值越大越难沟通。
    communicationDifficulty: Int,
    // 纠纷风险：0～1，数值越大越容易发生医疗纠纷。
    disputeRisk: Double
)

object PatientAttributes:
  given Codec.AsObject[PatientAttributes] = deriveCodec
