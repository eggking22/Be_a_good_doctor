import { APIMessage } from '@/system/api/APIMessage'

import type { GameState } from '@/objects/game/GameState'

export class StartGameAPIMessage extends APIMessage<GameState> {
  readonly playerName: string

  constructor(playerName: string) {
    super()
    this.playerName = playerName
  }

  get serviceName(): string {
    return 'game'
  }
}
