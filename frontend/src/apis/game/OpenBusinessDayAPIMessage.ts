import { APIMessage } from '@/system/api/APIMessage'

import type { GameState } from '@/objects/game/GameState'

export class OpenBusinessDayAPIMessage extends APIMessage<GameState> {
  readonly gameId: string

  constructor(gameId: string) {
    super()
    this.gameId = gameId
  }

  get serviceName(): string {
    return 'game'
  }
}
