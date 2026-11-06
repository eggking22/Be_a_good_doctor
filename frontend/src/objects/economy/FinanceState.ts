import type { CashFlow } from '@/objects/economy/CashFlow'
import type { DailyStatement } from '@/objects/economy/DailyStatement'

export interface FinanceState {
  cash: number
  cashFlow: CashFlow | null
  dailyStatement: DailyStatement | null
}
