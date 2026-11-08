import type { RecoveryFollowUp } from '@/objects/reputation/RecoveryFollowUp'
import { APIMessage } from '@/system/api/APIMessage'

export class TryRecoveryFollowUpAPIMessage extends APIMessage<RecoveryFollowUp | null> {
  readonly gameId: string

  constructor(gameId: string) {
    super()
    this.gameId = gameId
  }

  get serviceName(): string {
    return 'reputation'
  }
}
