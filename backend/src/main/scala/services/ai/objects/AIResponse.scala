// AI 适配对象：描述模型输出，不包含任何直接修改现金、库存或声誉的字段。
package services.ai.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import services.patient.objects.PatientEmotion

final case class AIResponse(
    text: String,
    emotion: PatientEmotion,
    communicationScore: Double,
    modelName: String
)

object AIResponse:
  given Codec.AsObject[AIResponse] = deriveCodec
