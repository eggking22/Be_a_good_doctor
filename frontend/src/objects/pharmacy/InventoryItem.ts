import type { Medicine } from '@/objects/pharmacy/Medicine'

export interface InventoryItem {
  id: string
  medicine: Medicine
  quantity: number
  expiryDate: string
}
