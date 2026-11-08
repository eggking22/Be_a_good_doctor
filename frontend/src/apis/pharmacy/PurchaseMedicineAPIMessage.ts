import { APIMessage } from '@/system/api/APIMessage'

import type { PharmacyOverview } from '@/objects/pharmacy/PharmacyOverview'

export class PurchaseMedicineAPIMessage extends APIMessage<PharmacyOverview> {
  readonly gameId: string
  readonly medicineId: string
  readonly quantity: number

  constructor(gameId: string, medicineId: string, quantity: number) {
    super()
    this.gameId = gameId
    this.medicineId = medicineId
    this.quantity = quantity
  }

  get serviceName(): string {
    return 'pharmacy'
  }
}
