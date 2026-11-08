import type { Inventory } from '@/objects/pharmacy/Inventory'
import type { Medicine } from '@/objects/pharmacy/Medicine'

export interface PharmacyOverview {
  gameId: string
  cash: number
  currentMinute: number
  canPurchase: boolean
  inventory: Inventory
  marketMedicines: Medicine[]
}
