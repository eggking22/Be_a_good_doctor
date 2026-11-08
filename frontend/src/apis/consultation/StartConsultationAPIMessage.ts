import type { StartConsultationResponse } from '@/objects/consultation/apiTypes/StartConsultationResponse'
import { APIMessage } from '@/system/api/APIMessage'

export class StartConsultationAPIMessage extends APIMessage<StartConsultationResponse> {
  readonly gameId: string
  readonly patientId: string

  constructor(gameId: string, patientId: string) {
    super()
    this.gameId = gameId
    this.patientId = patientId
  }

  get serviceName(): string {
    return 'consultation'
  }
}
