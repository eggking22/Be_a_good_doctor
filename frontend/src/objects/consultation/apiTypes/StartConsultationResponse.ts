import type { ConsultationStatus } from '@/objects/consultation/ConsultationStatus'
import type { PatientDialogueResponse } from '@/objects/consultation/apiTypes/PatientDialogueResponse'
import type { Patient } from '@/objects/patient/Patient'
import type { PatientEmotion } from '@/objects/patient/PatientEmotion'

export interface StartConsultationResponse {
  consultationId: string
  patient: Patient
  emotion: PatientEmotion
  status: ConsultationStatus
  opening: PatientDialogueResponse
}
