import type { GameState } from '@/objects/game/GameState'

export type ClinicPanel = 'consultation' | 'pharmacy' | 'staff' | 'finance' | 'expansion'

// 明确表达患者在诊室中的生命周期，避免只用布尔值隐藏进出场过程。
export type PatientStage = 'outside' | 'entering' | 'consulting' | 'leaving'

export type ClinicHomeStatus =
  | { status: 'ready'; game: GameState; season: string; gameTime: string; isOpen: boolean }
  | { status: 'empty'; message: string }
  | { status: 'error'; message: string }

export interface ClinicHomeInteractionState {
  activePanel: ClinicPanel | null
  patientStage: PatientStage
}
