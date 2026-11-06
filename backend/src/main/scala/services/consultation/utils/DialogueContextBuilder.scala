// 对话上下文工具：负责限制上下文长度和组织消息，不负责判定治疗结果。
package services.consultation.utils

import services.consultation.objects.DialogueMessage

object DialogueContextBuilder:
  private[consultation] def latest(messages: List[DialogueMessage], maxMessages: Int = 20): List[DialogueMessage] =
    messages.takeRight(maxMessages.max(1))
