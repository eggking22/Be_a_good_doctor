import type { PatientEmotion } from '@/objects/patient/PatientEmotion'

// 与后端 AIResponse 对齐：AI 只返回表达和沟通评价，不直接修改经营数据。
export interface AIResponse {
  text: string
  emotion: PatientEmotion
  communicationScore: number
  modelName: string
}
