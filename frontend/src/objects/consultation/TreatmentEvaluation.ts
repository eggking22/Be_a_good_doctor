// 与后端 TreatmentEvaluation 对齐：保存治疗方案的评价结果。
export interface TreatmentEvaluation {
  matched: boolean
  reputationDelta: number
  reason: string
}
