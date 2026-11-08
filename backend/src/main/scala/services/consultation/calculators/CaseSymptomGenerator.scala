// 病例症状生成器：将疾病目录症状转换为当前病例的隐藏症状。
package services.consultation.calculators

import services.consultation.objects.CaseSymptom
import services.pharmacy.objects.Symptom

object CaseSymptomGenerator:
  private[consultation] def generate(symptoms: List[Symptom], diseaseDifficulty: Int): List[CaseSymptom] =
    val severity = diseaseDifficulty.max(1).min(5)
    symptoms.map(symptom => CaseSymptom(symptom, severity, revealed = false))
