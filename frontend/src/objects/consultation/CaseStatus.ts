export const CaseStatuses = {
  Active: 'Active',
  Completed: 'Completed',
  FollowUpPlanned: 'FollowUpPlanned',
  Abandoned: 'Abandoned',
} as const

export type CaseStatus = (typeof CaseStatuses)[keyof typeof CaseStatuses]
