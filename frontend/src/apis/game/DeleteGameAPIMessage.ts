import { APIMessage } from '@/system/api/APIMessage'

import type { DeleteGameResponse } from '@/objects/game/apiTypes/DeleteGameResponse'

export class DeleteGameAPIMessage extends APIMessage<DeleteGameResponse> {
  readonly gameId: string

  constructor(gameId: string) {
    super()
    this.gameId = gameId
  }

  get serviceName(): string {
    return 'game'
  }
}
