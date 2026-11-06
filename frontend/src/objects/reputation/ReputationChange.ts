// 与后端 ReputationChange 对齐：当前声誉与历史变化记录分离。
export interface ReputationChange {
  amount: number
  reason: string
  changedAt: string
}
