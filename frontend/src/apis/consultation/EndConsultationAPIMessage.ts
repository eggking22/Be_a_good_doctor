import type { EndConsultationResponse } from '@/objects/consultation/apiTypes/EndConsultationResponse'
import { APIMessage } from '@/system/api/APIMessage'

export class EndConsultationAPIMessage extends APIMessage<EndConsultationResponse> {
  readonly consultationId: string

  constructor(consultationId: string) {
    super()
    this.consultationId = consultationId
  }

  get serviceName(): string {
    return 'consultation'
  }
}
