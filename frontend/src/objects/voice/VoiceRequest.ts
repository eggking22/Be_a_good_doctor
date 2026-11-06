// 与后端 VoiceRequest 对齐：描述一次 ASR/TTS 语音适配请求。
export interface VoiceRequest {
  sessionId: string
  language: string
  audioFormat: string
}
