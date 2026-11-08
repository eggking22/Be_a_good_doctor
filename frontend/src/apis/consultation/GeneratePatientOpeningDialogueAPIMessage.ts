import type { PatientDialogueResponse } from '@/objects/consultation/apiTypes/PatientDialogueResponse'
import { APIMessage } from '@/system/api/APIMessage'

export class GeneratePatientOpeningDialogueAPIMessage extends APIMessage<PatientDialogueResponse> {
  readonly consultationId: string

  constructor(consultationId: string) {
    super()
    this.consultationId = consultationId
  }

  get serviceName(): string {
    return 'consultation'
  }
}
