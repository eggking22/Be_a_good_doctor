import type { PlayerTextDialogueResponse } from '@/objects/consultation/apiTypes/PlayerTextDialogueResponse'
import { APIMessage } from '@/system/api/APIMessage'

export class SubmitPlayerTextAPIMessage extends APIMessage<PlayerTextDialogueResponse> {
  readonly consultationId: string
  readonly text: string

  constructor(consultationId: string, text: string) {
    super()
    this.consultationId = consultationId
    this.text = text
  }

  get serviceName(): string {
    return 'consultation'
  }
}
