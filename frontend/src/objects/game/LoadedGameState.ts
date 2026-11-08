import type { Consultation } from '@/objects/consultation/Consultation'
import type { ConsultationSnapshot } from '@/objects/consultation/ConsultationSnapshot'
import type { PatientCase } from '@/objects/consultation/PatientCase'
import type { DailyStatementRecord } from '@/objects/economy/DailyStatementRecord'
import type { LedgerTransaction } from '@/objects/economy/LedgerTransaction'
import type { GameEvent } from '@/objects/event/GameEvent'
import type { GameState } from '@/objects/game/GameState'
import type { Patient } from '@/objects/patient/Patient'
import type { ReputationChange } from '@/objects/reputation/ReputationChange'

// 与后端 LoadedGameState 对齐：GameState 聚合与数据库历史集合分开返回。
export interface LoadedGameState {
  state: GameState
  patients: Patient[]
  patientCases: PatientCase[]
  consultations: Consultation[]
  consultationSnapshots: ConsultationSnapshot[]
  events: GameEvent[]
  reputationHistory: ReputationChange[]
  dailyStatements: DailyStatementRecord[]
  ledgerTransactions: LedgerTransaction[]
}
