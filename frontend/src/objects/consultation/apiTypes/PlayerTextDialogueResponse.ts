import type { DialogueMessage } from '@/objects/consultation/DialogueMessage'
import type { ConsultationEndReason } from '@/objects/consultation/ConsultationEndReason'
import type { ConsultationStatus } from '@/objects/consultation/ConsultationStatus'
import type { PlayerTextAnalysis } from '@/objects/consultation/PlayerTextAnalysis'
import type { PrescriptionItem } from '@/objects/consultation/PrescriptionItem'

export interface PlayerTextDialogueResponse {
  playerMessage: DialogueMessage
  patientMessage: DialogueMessage | null
  analysis: PlayerTextAnalysis
  shoppingList: PrescriptionItem[]
  status: ConsultationStatus
  endReason: ConsultationEndReason | null
  modelName: string
}
