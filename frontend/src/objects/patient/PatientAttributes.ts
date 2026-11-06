// 与后端 PatientAttributes 对齐：属性名称和数值类型必须保持一致。
export interface PatientAttributes {
  illnessDifficulty: number
  paymentWillingness: number
  communicationDifficulty: number
  disputeRisk: number
}
