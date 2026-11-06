export const StaffRoles = {
  Doctor: 'Doctor',
  Nurse: 'Nurse',
  Receptionist: 'Receptionist',
  SecurityGuard: 'SecurityGuard',
  AIAssistant: 'AIAssistant',
} as const

export type StaffRole = (typeof StaffRoles)[keyof typeof StaffRoles]
