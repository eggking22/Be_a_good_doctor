import type { PatientAttributes } from '@/objects/patient/PatientAttributes'

// 与后端 Patient 对齐：患者的经营属性集中在 PatientAttributes 中。
export interface Patient {
  id: string
  name: string
  attributes: PatientAttributes
}
