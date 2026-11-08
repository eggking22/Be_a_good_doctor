// AI 结果校验：阻止模型编造症状或药品编号进入病例和后续结算流程。
package services.consultation.utils

import services.ai.objects.GeneratedPatientDialogue
import services.consultation.objects.{PatientConversationAction, PlayerTextAnalysis}

object DialogueAIResultValidator:
  private[consultation] def patientText(dialogue: GeneratedPatientDialogue): Either[String, String] =
    val text = dialogue.text.trim
    if text.length < 2 || text.length > 160 then Left("患者台词长度必须在 2 到 160 个字符之间。")
    else Right(text)

  private[consultation] def mentionedSymptomIds(
      dialogue: GeneratedPatientDialogue,
      allowedIds: Set[String]
  ): Either[String, List[String]] =
    val ids = dialogue.mentionedSymptomIds.distinct
    val unknownIds = ids.filterNot(allowedIds.contains)
    if unknownIds.nonEmpty then Left(s"模型返回了未授权的症状编号：${unknownIds.mkString(", ")}")
    else Right(ids)

  private[consultation] def playerAnalysis(
      dialogue: GeneratedPatientDialogue,
      allowedMedicineIds: Set[String]
  ): Either[String, PlayerTextAnalysis] =
    dialogue.playerTextAnalysis.toRight("模型未返回玩家文字理解结果。").flatMap { analysis =>
      val unknownIds = analysis.mentionedMedicineIds.distinct.filterNot(allowedMedicineIds.contains)
      if unknownIds.nonEmpty then Left(s"模型返回了未授权的药品编号：${unknownIds.mkString(", ")}")
      else if analysis.confidence.isNaN || analysis.confidence.isInfinity || analysis.confidence < 0 || analysis.confidence > 1 then
        Left("模型返回的文字理解置信度必须介于 0 到 1。")
      else Right(analysis.copy(mentionedMedicineIds = analysis.mentionedMedicineIds.distinct))
    }

  private[consultation] def patientConversationAction(
      dialogue: GeneratedPatientDialogue,
      recognizedMedicineIds: List[String],
      hasExistingPrescription: Boolean = false
  ): Either[String, PatientConversationAction] =
    (recognizedMedicineIds.nonEmpty, hasExistingPrescription, dialogue.patientConversationAction) match
      case (false, false, PatientConversationAction.Continue) => Right(PatientConversationAction.Continue)
      case (false, false, action) => Left(s"本次接诊尚未识别药品，患者对话动作只能是 Continue，实际为：$action")
      case (true, _, PatientConversationAction.Continue) => Left("本轮已识别药品，患者需要追问注意事项或主动结束聊天。")
      case _ => Right(dialogue.patientConversationAction)
