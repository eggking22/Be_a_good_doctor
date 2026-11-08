// 玩家文字分析：AI 的受限提取结果，不能直接修改声誉、库存或金额。
package services.consultation.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class PlayerTextAnalysis(
    attitude: PlayerAttitude,
    intent: PlayerIntent,
    mentionedMedicineIds: List[String],
    confidence: Double
)

object PlayerTextAnalysis:
  given Codec.AsObject[PlayerTextAnalysis] = deriveCodec
