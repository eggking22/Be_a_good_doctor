// AI 对话可见药品：只保留识别药名所需的编号和名称，不暴露价格。
package services.ai.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class DialogueMedicine(id: String, name: String)

object DialogueMedicine:
  given Codec.AsObject[DialogueMedicine] = deriveCodec
