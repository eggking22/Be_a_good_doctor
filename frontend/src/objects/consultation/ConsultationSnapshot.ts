import type { Consultation } from '@/objects/consultation/Consultation'
import type { PatientCase } from '@/objects/consultation/PatientCase'
import type { PlayerTextAnalysis } from '@/objects/consultation/PlayerTextAnalysis'
import type { PrescriptionItem } from '@/objects/consultation/PrescriptionItem'

// 与后端 ConsultationSnapshot 对齐，用于完整恢复一局游戏的接诊上下文。
export interface ConsultationSnapshot {
  consultation: Consultation
  patientCase: PatientCase | null
  playerTextAnalyses: PlayerTextAnalysis[]
  prescriptionItems: PrescriptionItem[]
}
