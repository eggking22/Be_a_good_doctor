// 症状透露规则：由后端决定本轮可说的症状，模型只能在该范围内组织语言。
package services.consultation.utils

import services.consultation.objects.CaseSymptom

object SymptomRevealSelector:
  private[consultation] def opening(symptoms: List[CaseSymptom], communicationDifficulty: Int): List[CaseSymptom] =
    val count = if communicationDifficulty <= 2 then 2 else 1
    symptoms.filterNot(_.revealed).take(count)

  private[consultation] def reply(symptoms: List[CaseSymptom]): List[CaseSymptom] =
    val alreadyRevealed = symptoms.filter(_.revealed)
    val nextUnrevealed = symptoms.filterNot(_.revealed).take(1)
    (alreadyRevealed ++ nextUnrevealed).distinctBy(_.symptom.id)
