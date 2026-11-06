// 与后端 VoiceText 对齐：保存语音识别文本和置信度。
export interface VoiceText {
  text: string
  confidence: number
}
