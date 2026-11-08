import type { GameState } from '@/objects/game/GameState'
import type { ClinicConversation } from '@/pages/ClinicHomePage/objects/ClinicConversation'
import type { PatientPortrait } from '@/pages/ClinicHomePage/objects/PatientPortrait'
import type { PharmacyOverview } from '@/objects/pharmacy/PharmacyOverview'
import type { RecoveryFollowUp } from '@/objects/reputation/RecoveryFollowUp'

export type ClinicPanel = 'consultation' | 'pharmacy' | 'staff' | 'finance' | 'expansion'

// 明确表达患者在诊室中的生命周期，避免只用布尔值隐藏进场过程。
export type PatientStage = 'outside' | 'entering' | 'consulting' | 'ended'

export type ClinicHomeStatus =
  | { status: 'loading' }
  | { status: 'ready'; game: GameState }
  | { status: 'empty'; message: string }
  | { status: 'error'; message: string }

export interface ClinicHomeInteractionState {
  activePanel: ClinicPanel | null
  patientStage: PatientStage
  patientPortrait: PatientPortrait | null
  conversation: ClinicConversation | null
  isStartingConsultation: boolean
  isSendingText: boolean
  isEndingConsultation: boolean
  isSettlingDay: boolean
  pharmacyOverview: PharmacyOverview | null
  isLoadingPharmacy: boolean
  purchasingMedicineId: string | null
  recoveryFollowUp: RecoveryFollowUp | null
  consultationError: string | null
}
