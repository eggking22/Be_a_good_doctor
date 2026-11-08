import type { Disease } from '@/objects/pharmacy/Disease'
import type { MedicalCatalogDiseaseMedicine } from '@/objects/pharmacy/MedicalCatalogDiseaseMedicine'
import type { MedicalCatalogDiseaseSymptom } from '@/objects/pharmacy/MedicalCatalogDiseaseSymptom'
import type { Medicine } from '@/objects/pharmacy/Medicine'
import type { Symptom } from '@/objects/pharmacy/Symptom'

export interface MedicalCatalog {
  diseases: Disease[]
  symptoms: Symptom[]
  medicines: Medicine[]
  diseaseSymptoms: MedicalCatalogDiseaseSymptom[]
  diseaseMedicines: MedicalCatalogDiseaseMedicine[]
}
