import type { ConsultationEndReason } from '@/objects/consultation/ConsultationEndReason'
import type { ConsultationStatus } from '@/objects/consultation/ConsultationStatus'
import type { DialogueMessage } from '@/objects/consultation/DialogueMessage'
import type { PlayerTextAnalysis } from '@/objects/consultation/PlayerTextAnalysis'
import type { PrescriptionItem } from '@/objects/consultation/PrescriptionItem'

export interface EndConsultationResponse {
  playerMessage: DialogueMessage
  patientMessage: DialogueMessage
  analysis: PlayerTextAnalysis
  status: ConsultationStatus
  endReason: ConsultationEndReason
  shoppingList: PrescriptionItem[]
  modelName: string
}
