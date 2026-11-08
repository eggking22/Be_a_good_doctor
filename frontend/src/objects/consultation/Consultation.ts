import type { Patient } from '@/objects/patient/Patient'
import type { PatientEmotion } from '@/objects/patient/PatientEmotion'
import type { ConsultationStatus } from '@/objects/consultation/ConsultationStatus'
import type { DialogueMessage } from '@/objects/consultation/DialogueMessage'
import type { TreatmentEvaluation } from '@/objects/consultation/TreatmentEvaluation'

// 与后端 Consultation 对齐：一次接诊过程不保存 gameId。
export interface Consultation {
  id: string
  caseId: string | null
  patient: Patient
  emotion: PatientEmotion
  status: ConsultationStatus
  dialogueHistory: DialogueMessage[]
  dialogueCount: number
  treatmentEvaluation: TreatmentEvaluation | null
  cashChange: number
  reputationChange: number
}
