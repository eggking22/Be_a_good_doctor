// 与后端 PatientEmotion 枚举保持同名、同值。
export const PatientEmotions = {
  Calm: 'Calm',
  Angry: 'Angry',
  Anxious: 'Anxious',
  Happy: 'Happy',
} as const

export type PatientEmotion = (typeof PatientEmotions)[keyof typeof PatientEmotions]
