// 生成患者开场话术：后端先决定可透露症状，再交给 AI 组织成自然表达。
package services.consultation.api

import cats.effect.IO
import cats.syntax.all.*
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import services.ai.api.GeneratePatientDialogueAPIMessage
import services.consultation.objects.{ConsultationStatus, DialogueMessage, PatientConversationAction}
import services.consultation.objects.apiTypes.PatientDialogueResponse
import services.consultation.tables.{CaseSymptomTable, ConsultationTable, DialogueMessageTable, PatientCaseTable}
import services.consultation.utils.{DialogueAIContextBuilder, DialogueAIResultValidator, SymptomRevealSelector}
import services.pharmacy.tables.MedicineTable
import services.system.HttpError
import services.system.api.APIMessage

import java.sql.Connection
import java.time.OffsetDateTime

final case class GeneratePatientOpeningDialogueAPIMessage(
    consultationId: String
) extends APIMessage[PatientDialogueResponse]:
  override def plan(connection: Connection): IO[PatientDialogueResponse] =
    val normalizedConsultationId = consultationId.trim
    if normalizedConsultationId.isEmpty then IO.raiseError(HttpError.BadRequest("接诊 ID 不能为空。"))
    else
      for
        consultation <- ConsultationTable.findById(connection, normalizedConsultationId).flatMap {
          case Some(value) => IO.pure(value)
          case None => IO.raiseError(HttpError.NotFound(s"未找到接诊记录：$normalizedConsultationId"))
        }
        _ <- if consultation.status == ConsultationStatus.InProgress then IO.unit
        else IO.raiseError(HttpError.Conflict("当前接诊不处于进行中状态。"))
        _ <- if consultation.dialogueHistory.exists(_.speaker.equalsIgnoreCase("patient")) then
          IO.raiseError(HttpError.Conflict("该接诊已生成患者开场话术。"))
        else IO.unit
        caseId <- IO.fromOption(consultation.caseId)(HttpError.Conflict("接诊未关联患者病例。"))
        patientCase <- PatientCaseTable.findById(connection, caseId).flatMap {
          case Some(value) => IO.pure(value)
          case None => IO.raiseError(HttpError.NotFound(s"未找到病例：$caseId"))
        }
        symptomsAllowed = SymptomRevealSelector.opening(patientCase.symptoms, consultation.patient.attributes.communicationDifficulty)
        _ <- if symptomsAllowed.nonEmpty then IO.unit
        else IO.raiseError(HttpError.Conflict("该病例没有可用于开场陈述的症状。"))
        medicines <- MedicineTable.findAll(connection)
        generated <- GeneratePatientDialogueAPIMessage(
          DialogueAIContextBuilder.build(consultation, symptomsAllowed, medicines, consultation.dialogueHistory, None)
        ).plan(connection)
        text <- IO.fromEither(DialogueAIResultValidator.patientText(generated).left.map(HttpError.Conflict.apply))
        symptomIds <- IO.fromEither(
          DialogueAIResultValidator.mentionedSymptomIds(generated, symptomsAllowed.map(_.symptom.id).toSet).left.map(HttpError.Conflict.apply)
        )
        _ <- if symptomIds.nonEmpty then IO.unit
        else IO.raiseError(HttpError.Conflict("患者开场话术没有提及允许透露的症状。"))
        _ <- if generated.playerTextAnalysis.isEmpty then IO.unit
        else IO.raiseError(HttpError.Conflict("开场话术不应包含玩家文字分析。"))
        _ <- if generated.patientConversationAction == PatientConversationAction.Continue then IO.unit
        else IO.raiseError(HttpError.Conflict("开场话术不能结束或追问药品。"))
        _ <- if !generated.shouldLeave then IO.unit
        else IO.raiseError(HttpError.Conflict("开场话术不能要求患者离场。"))
        patientMessage = DialogueMessage("patient", text, OffsetDateTime.now().toString)
        _ <- DialogueMessageTable.append(connection, consultation.id, patientMessage)
        _ <- symptomIds.traverse_(symptomId => CaseSymptomTable.updateRevealed(connection, caseId, symptomId, revealed = true))
        _ <- ConsultationTable.update(connection, consultation.copy(dialogueCount = consultation.dialogueCount + 1))
      yield PatientDialogueResponse(patientMessage, generated.modelName)

object GeneratePatientOpeningDialogueAPIMessage:
  given Codec.AsObject[GeneratePatientOpeningDialogueAPIMessage] = deriveCodec
