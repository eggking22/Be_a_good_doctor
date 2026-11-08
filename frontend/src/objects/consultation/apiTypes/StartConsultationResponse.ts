import type { ConsultationStatus } from '@/objects/consultation/ConsultationStatus'
import type { Patient } from '@/objects/patient/Patient'
import type { PatientEmotion } from '@/objects/patient/PatientEmotion'

export interface StartConsultationResponse {
  consultationId: string
  patient: Patient
  emotion: PatientEmotion
  status: ConsultationStatus
}
