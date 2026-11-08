import type { PlayerAttitude } from '@/objects/consultation/PlayerAttitude'
import type { PlayerIntent } from '@/objects/consultation/PlayerIntent'

export interface PlayerTextAnalysis {
  attitude: PlayerAttitude
  intent: PlayerIntent
  mentionedMedicineIds: string[]
  confidence: number
}
