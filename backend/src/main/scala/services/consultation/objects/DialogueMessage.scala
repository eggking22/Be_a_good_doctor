// 对话消息对象：保存一次玩家或患者的文本消息。
package services.consultation.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class DialogueMessage(
    speaker: String,
    content: String,
    createdAt: String
)

object DialogueMessage:
  given Codec.AsObject[DialogueMessage] = deriveCodec
