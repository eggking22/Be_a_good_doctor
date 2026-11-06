// 治疗评价对象：保存游戏规则对治疗方案的判断结果。
package services.consultation.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class TreatmentEvaluation(
    matched: Boolean,
    reputationDelta: Double,
    reason: String
)

object TreatmentEvaluation:
  given Codec.AsObject[TreatmentEvaluation] = deriveCodec
