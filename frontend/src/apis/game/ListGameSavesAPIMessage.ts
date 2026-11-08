import { APIMessage } from '@/system/api/APIMessage'

import type { GameSaveSummary } from '@/objects/game/GameSaveSummary'

export class ListGameSavesAPIMessage extends APIMessage<GameSaveSummary[]> {
  get serviceName(): string {
    return 'game'
  }
}
