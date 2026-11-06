// 与后端 EventType 枚举保持同名、同值。
export const EventTypes = {
  MedicalDispute: 'MedicalDispute',
  SecurityIncident: 'SecurityIncident',
  EmployeeIncident: 'EmployeeIncident',
  SeasonalEvent: 'SeasonalEvent',
  MarketEvent: 'MarketEvent',
} as const

export type EventType = (typeof EventTypes)[keyof typeof EventTypes]
