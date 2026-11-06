// 与后端 ConsultationStatus 枚举保持同名、同值，便于 JSON 传输。
export const ConsultationStatuses = {
  Waiting: 'Waiting',
  InProgress: 'InProgress',
  Completed: 'Completed',
  Referred: 'Referred',
  Abandoned: 'Abandoned',
} as const

export type ConsultationStatus = (typeof ConsultationStatuses)[keyof typeof ConsultationStatuses]
