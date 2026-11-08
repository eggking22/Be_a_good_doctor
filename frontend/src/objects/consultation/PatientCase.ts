import type { CaseStatus } from '@/objects/consultation/CaseStatus'
import type { CaseSymptom } from '@/objects/consultation/CaseSymptom'
import type { Disease } from '@/objects/pharmacy/Disease'

export interface PatientCase {
  id: string
  patientId: string
  disease: Disease
  symptoms: CaseSymptom[]
  status: CaseStatus
}
