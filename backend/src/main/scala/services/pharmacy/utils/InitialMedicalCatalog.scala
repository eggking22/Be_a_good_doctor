// 游戏示例数据：基础对象和关系分开列出，后续可按相同格式扩充。
package services.pharmacy.utils

import services.pharmacy.objects.{Disease, DiseaseAttributes, Medicine, MedicineLevel, Symptom}

private[pharmacy] object InitialMedicalCatalog:
  private val runnyNose = Symptom("symptom-runny-nose", "流鼻涕")
  private val soreThroat = Symptom("symptom-sore-throat", "嗓子疼")
  private val fever = Symptom("symptom-fever", "发热")
  private val cough = Symptom("symptom-cough", "咳嗽")
  private val headache = Symptom("symptom-headache", "头痛")
  private val chestDiscomfort = Symptom("symptom-chest-discomfort", "胸闷")
  private val cold = Disease("disease-common-cold", "感冒", DiseaseAttributes(1, 0.7))
  private val allergicRhinitis = Disease("disease-allergic-rhinitis", "过敏性鼻炎", DiseaseAttributes(1, 0.3))
  private val flu = Disease("disease-seasonal-flu", "流感", DiseaseAttributes(3, 1.0))
  private val respiratoryInfection = Disease("disease-severe-respiratory-infection", "重度呼吸道感染", DiseaseAttributes(5, 1.0))
  private val coldMedicine = Medicine("medicine-cold", "感冒药", InitialMedicinePrices.coldMedicine, MedicineLevel.EasyDrug)
  private val fluMedicine = Medicine("medicine-flu", "流感对症药", InitialMedicinePrices.fluMedicine, MedicineLevel.Ordinary)
  private val respiratoryMedicine = Medicine("medicine-respiratory", "呼吸道特效药", InitialMedicinePrices.respiratoryMedicine, MedicineLevel.HighAlert)

  val symptoms: List[Symptom] = List(runnyNose, soreThroat, fever, cough, headache, chestDiscomfort)
  val diseases: List[Disease] = List(cold, allergicRhinitis, flu, respiratoryInfection)
  val medicines: List[Medicine] = List(coldMedicine, fluMedicine, respiratoryMedicine)
  val diseaseSymptoms: List[(String, String)] = List(
    (cold.id, runnyNose.id),
    (cold.id, soreThroat.id),
    (allergicRhinitis.id, runnyNose.id),
    (allergicRhinitis.id, soreThroat.id),
    (flu.id, fever.id),
    (flu.id, cough.id),
    (flu.id, headache.id),
    (respiratoryInfection.id, fever.id),
    (respiratoryInfection.id, cough.id),
    (respiratoryInfection.id, chestDiscomfort.id)
  )
  val diseaseMedicines: List[(String, String)] = List(
    (cold.id, coldMedicine.id),
    (allergicRhinitis.id, coldMedicine.id),
    (flu.id, fluMedicine.id),
    (respiratoryInfection.id, respiratoryMedicine.id)
  )
