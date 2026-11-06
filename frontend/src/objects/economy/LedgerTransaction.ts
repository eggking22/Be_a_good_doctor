// 与后端 LedgerTransaction 对齐：一笔收入或支出流水，不属于 GameState 当前聚合。
export interface LedgerTransaction {
  id: string
  gameId: string
  transactionType: string
  amount: number
  occurredOn: number
  description: string
}
