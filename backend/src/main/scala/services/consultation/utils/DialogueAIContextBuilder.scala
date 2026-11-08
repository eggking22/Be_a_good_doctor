// AI 上下文组装：只传递症状快照、患者公开属性和最近对话。
package services.consultation.utils

import services.ai.objects.{DialogueMedicine, DialogueSymptom, PatientDialogueContext}
import services.consultation.objects.{CaseSymptom, Consultation, DialogueMessage}
import services.pharmacy.objects.Medicine

object DialogueAIContextBuilder:
  private[consultation] def build(
      consultation: Consultation,
      symptomsAllowed: List[CaseSymptom],
      medicines: List[Medicine],
      messages: List[DialogueMessage],
      playerText: Option[String],
      hasExistingPrescription: Boolean = false
  ): PatientDialogueContext =
    PatientDialogueContext(
      patientName = consultation.patient.name,
      communicationDifficulty = consultation.patient.attributes.communicationDifficulty,
      emotion = consultation.emotion,
      symptomsAllowed = symptomsAllowed.map(symptom => DialogueSymptom(symptom.symptom.id, symptom.symptom.name, symptom.severity)),
      medicinesAllowed = medicines.map(medicine => DialogueMedicine(medicine.id, medicine.name)),
      recentMessages = DialogueContextBuilder.latest(messages, maxMessages = 10),
      playerText = playerText,
      hasExistingPrescription = hasExistingPrescription
    )
