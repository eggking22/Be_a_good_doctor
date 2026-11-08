import { APIMessage } from '@/system/api/APIMessage'

import type { GameState } from '@/objects/game/GameState'

export class StartGameAPIMessage extends APIMessage<GameState> {
  readonly playerName: string
  readonly replaceGameId?: string

  constructor(playerName: string, replaceGameId?: string) {
    super()
    this.playerName = playerName
    this.replaceGameId = replaceGameId
  }

  get serviceName(): string {
    return 'game'
  }
}
