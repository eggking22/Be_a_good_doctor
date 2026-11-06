import type { Facility } from '@/objects/clinic/Facility'
import type { Staff } from '@/objects/clinic/Staff'

export interface Clinic {
  facilities: Facility[]
  employees: Staff[]
}
