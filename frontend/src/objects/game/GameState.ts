import type { Clinic } from '@/objects/clinic/Clinic'
import type { FinanceState } from '@/objects/economy/FinanceState'
import type { Inventory } from '@/objects/pharmacy/Inventory'
import type { Reputation } from '@/objects/reputation/Reputation'

export interface GameState {
  id: string
  playerName: string
  currentDay: number
  finance: FinanceState
  reputation: Reputation
  clinic: Clinic
  inventory: Inventory
  bankrupt: boolean
  createdAt: string
  updatedAt: string
}
