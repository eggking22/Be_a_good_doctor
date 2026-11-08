import type { Symptom } from '@/objects/pharmacy/Symptom'

export interface CaseSymptom {
  symptom: Symptom
  severity: number
  revealed: boolean
}
