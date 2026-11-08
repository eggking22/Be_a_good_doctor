// 对话结束规则：常规文字问诊最多允许十个玩家回合；语言结束意图由 AI 解析并保存。
package services.consultation.utils

import services.consultation.objects.DialogueMessage

object ConversationEndDetector:
  val maximumPlayerTurns: Int = 10

  private[consultation] def playerTurnCount(messages: List[DialogueMessage]): Int =
    messages.count(_.speaker.equalsIgnoreCase("player"))
