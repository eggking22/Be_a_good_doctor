import { APIMessage } from '@/system/api/APIMessage'

import type { LoadedGameState } from '@/objects/game/LoadedGameState'

export class LoadGameAPIMessage extends APIMessage<LoadedGameState> {
  readonly gameId: string

  constructor(gameId: string) {
    super()
    this.gameId = gameId
  }

  get serviceName(): string {
    return 'game'
  }
}
