import { APIMessage } from '@/system/api/APIMessage'

import type { PharmacyOverview } from '@/objects/pharmacy/PharmacyOverview'

export class GetPharmacyOverviewAPIMessage extends APIMessage<PharmacyOverview> {
  readonly gameId: string

  constructor(gameId: string) {
    super()
    this.gameId = gameId
  }

  get serviceName(): string {
    return 'pharmacy'
  }
}
