import type { DialogueMessage } from '@/objects/consultation/DialogueMessage'
import type { ConsultationEndReason } from '@/objects/consultation/ConsultationEndReason'
import type { PlayerTextAnalysis } from '@/objects/consultation/PlayerTextAnalysis'
import type { PrescriptionItem } from '@/objects/consultation/PrescriptionItem'
import type { Patient } from '@/objects/patient/Patient'

export interface ClinicConversation {
  consultationId: string
  patient: Patient
  messages: DialogueMessage[]
  latestAnalysis: PlayerTextAnalysis | null
  shoppingList: PrescriptionItem[]
  endReason: ConsultationEndReason | null
}
