import type { StaffRole } from '@/objects/clinic/StaffRole'

export interface Staff {
  id: string
  name: string
  role: StaffRole
  dailySalary: number
  ability: number
  communicationAbility: number
  satisfaction: number
  active: boolean
}
