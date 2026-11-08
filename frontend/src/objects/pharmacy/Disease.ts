import type { DiseaseAttributes } from '@/objects/pharmacy/DiseaseAttributes'

export interface Disease {
  id: string
  name: string
  attributes: DiseaseAttributes
}
