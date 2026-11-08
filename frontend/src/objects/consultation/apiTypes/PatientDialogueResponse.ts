import type { DialogueMessage } from '@/objects/consultation/DialogueMessage'

export interface PatientDialogueResponse {
  patientMessage: DialogueMessage
  modelName: string
}
