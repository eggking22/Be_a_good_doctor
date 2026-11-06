// 与后端 DialogueMessage 对齐：保存玩家或患者的一条对话文本。
export interface DialogueMessage {
  speaker: string
  content: string
  createdAt: string
}
