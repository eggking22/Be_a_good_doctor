// AI 对话可提及症状：来自病例快照，不包含疾病诊断结果。
package services.ai.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class DialogueSymptom(id: String, name: String, severity: Int)

object DialogueSymptom:
  given Codec.AsObject[DialogueSymptom] = deriveCodec
