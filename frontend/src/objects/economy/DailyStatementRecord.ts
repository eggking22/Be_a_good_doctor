import type { DailyStatement } from '@/objects/economy/DailyStatement'

// 与后端 DailyStatementRecord 对齐：日结算的数据库历史记录。
export interface DailyStatementRecord {
  id: string
  gameId: string
  day: number
  statement: DailyStatement
  createdAt: string
}
