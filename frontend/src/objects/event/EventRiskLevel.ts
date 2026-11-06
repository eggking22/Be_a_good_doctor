// 与后端 EventRiskLevel 枚举保持同名、同值。
export const EventRiskLevels = {
  Low: 'Low',
  Medium: 'Medium',
  High: 'High',
} as const

export type EventRiskLevel = (typeof EventRiskLevels)[keyof typeof EventRiskLevels]
