import type { MedicinePrice } from '@/objects/pharmacy/MedicinePrice'
import type { MedicineLevel } from '@/objects/pharmacy/MedicineLevel'

export interface Medicine {
  id: string
  name: string
  price: MedicinePrice
  level: MedicineLevel
}
