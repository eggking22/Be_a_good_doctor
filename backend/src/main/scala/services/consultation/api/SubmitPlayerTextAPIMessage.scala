// 提交玩家文字：保存原话，识别药品写入购物清单，并依据患者收尾或回合上限结束文字接诊。
package services.consultation.api

import cats.effect.IO
import cats.syntax.all.*
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import services.ai.api.GeneratePatientDialogueAPIMessage
import services.consultation.objects.{Consultation, ConsultationEndReason, ConsultationStatus, DialogueMessage, PatientConversationAction}
import services.consultation.objects.apiTypes.PlayerTextDialogueResponse
import services.consultation.tables.{CaseSymptomTable, ConsultationTable, DialogueMessageTable, PatientCaseTable, PlayerTextAnalysisTable, PrescriptionItemTable}
import services.consultation.utils.{ConsultationSettlement, ConversationEndDetector, DialogueAIContextBuilder, DialogueAIResultValidator, MedicineNameMatcher, SymptomRevealSelector}
import services.game.calculators.GameClock
import services.game.tables.GameStateTable
import services.pharmacy.tables.MedicineTable
import services.system.HttpError
import services.system.api.APIMessage

import java.sql.Connection
import java.time.OffsetDateTime

final case class SubmitPlayerTextAPIMessage(
    consultationId: String,
    text: String
) extends APIMessage[PlayerTextDialogueResponse]:
  override def plan(connection: Connection): IO[PlayerTextDialogueResponse] =
    val normalizedConsultationId = consultationId.trim
    val normalizedText = text.trim
    if normalizedConsultationId.isEmpty || normalizedText.isEmpty then
      IO.raiseError(HttpError.BadRequest("接诊 ID 和玩家文字不能为空。"))
    else if normalizedText.length > 500 then
      IO.raiseError(HttpError.BadRequest("玩家文字不能超过 500 个字符。"))
    else
      for
        consultation <- ConsultationTable.findById(connection, normalizedConsultationId).flatMap {
          case Some(value) => IO.pure(value)
          case None => IO.raiseError(HttpError.NotFound(s"未找到接诊记录：$normalizedConsultationId"))
        }
        _ <- if consultation.status == ConsultationStatus.InProgress then IO.unit
        else IO.raiseError(HttpError.Conflict("当前接诊不处于进行中状态。"))
        _ <- if consultation.dialogueHistory.exists(_.speaker.equalsIgnoreCase("patient")) then IO.unit
        else IO.raiseError(HttpError.Conflict("请先生成患者开场话术。"))
        playerMessage = DialogueMessage("player", normalizedText, OffsetDateTime.now().toString)
        _ <- DialogueMessageTable.append(connection, consultation.id, playerMessage)
        response <- continueConversation(connection, consultation, playerMessage, normalizedText)
      yield response

  private def continueConversation(
      connection: Connection,
      consultation: Consultation,
      playerMessage: DialogueMessage,
      playerText: String
  ): IO[PlayerTextDialogueResponse] =
    for
      caseId <- IO.fromOption(consultation.caseId)(HttpError.Conflict("接诊未关联患者病例。"))
      patientCase <- PatientCaseTable.findById(connection, caseId).flatMap {
        case Some(value) => IO.pure(value)
        case None => IO.raiseError(HttpError.NotFound(s"未找到病例：$caseId"))
      }
      medicines <- MedicineTable.findAll(connection)
      existingShoppingList <- PrescriptionItemTable.findByConsultationId(connection, consultation.id)
      symptomsAllowed = SymptomRevealSelector.reply(patientCase.symptoms)
      generated <- GeneratePatientDialogueAPIMessage(
        DialogueAIContextBuilder.build(
          consultation,
          symptomsAllowed,
          medicines,
          consultation.dialogueHistory :+ playerMessage,
          Some(playerText),
          hasExistingPrescription = existingShoppingList.nonEmpty
        )
      ).plan(connection)
      patientText <- IO.fromEither(DialogueAIResultValidator.patientText(generated).left.map(HttpError.Conflict.apply))
      symptomIds <- IO.fromEither(
        DialogueAIResultValidator.mentionedSymptomIds(generated, symptomsAllowed.map(_.symptom.id).toSet).left.map(HttpError.Conflict.apply)
      )
      modelAnalysis <- IO.fromEither(
        DialogueAIResultValidator.playerAnalysis(generated, medicines.map(_.id).toSet).left.map(HttpError.Conflict.apply)
      )
      recognizedMedicineIds = (modelAnalysis.mentionedMedicineIds ++ MedicineNameMatcher.matchedMedicineIds(playerText, medicines)).distinct
      analysis = modelAnalysis.copy(mentionedMedicineIds = recognizedMedicineIds)
      action <- IO.fromEither(
        DialogueAIResultValidator
          .patientConversationAction(generated, recognizedMedicineIds, hasExistingPrescription = existingShoppingList.nonEmpty)
          .left
          .map(HttpError.Conflict.apply)
      )
      patientMessage = DialogueMessage("patient", patientText, OffsetDateTime.now().toString)
      _ <- PlayerTextAnalysisTable.append(connection, consultation.id, analysis)
      _ <- DialogueMessageTable.append(connection, consultation.id, patientMessage)
      _ <- recognizedMedicineIds.traverse_(medicineId => PrescriptionItemTable.addIfMissing(connection, consultation.id, medicineId))
      _ <- symptomIds.filter(symptomId => patientCase.symptoms.exists(symptom => symptom.symptom.id == symptomId && !symptom.revealed))
        .traverse_(symptomId => CaseSymptomTable.updateRevealed(connection, caseId, symptomId, revealed = true))
      playerTurns = ConversationEndDetector.playerTurnCount(consultation.dialogueHistory) + 1
      endReason = endReasonFor(action, generated.shouldLeave, playerTurns)
      status = if endReason.nonEmpty then ConsultationStatus.Completed else ConsultationStatus.InProgress
      updated = consultation.copy(status = status, dialogueCount = consultation.dialogueCount + 2)
      _ <- ConsultationTable.update(connection, updated)
      gameId <- ConsultationTable.findGameIdById(connection, consultation.id).flatMap {
        case Some(value) => IO.pure(value)
        case None => IO.raiseError(HttpError.NotFound(s"未找到接诊所属游戏：${consultation.id}"))
      }
      game <- GameStateTable.findById(connection, gameId).flatMap {
        case Some(value) => IO.pure(value)
        case None => IO.raiseError(HttpError.NotFound(s"未找到游戏：$gameId"))
      }
      advancedGame = game.copy(
        currentMinute = GameClock.advanceDialogueMinute(game.currentMinute),
        updatedAt = OffsetDateTime.now()
      )
      _ <- GameStateTable.update(connection, advancedGame)
      _ <- if status == ConsultationStatus.Completed then ConsultationSettlement.settleIfNeeded(connection, consultation.id) else IO.unit
      shoppingList <- PrescriptionItemTable.findByConsultationId(connection, consultation.id)
    yield PlayerTextDialogueResponse(
      playerMessage = playerMessage,
      patientMessage = Some(patientMessage),
      analysis = analysis,
      shoppingList = shoppingList,
      status = status,
      endReason = endReason,
      modelName = generated.modelName
    )

  private def endReasonFor(
      action: PatientConversationAction,
      shouldLeave: Boolean,
      playerTurns: Int
  ): Option[ConsultationEndReason] =
    if action == PatientConversationAction.Leave then Some(ConsultationEndReason.PatientLeft)
    else if shouldLeave then Some(ConsultationEndReason.PlayerRequested)
    else if playerTurns >= ConversationEndDetector.maximumPlayerTurns then Some(ConsultationEndReason.RoundLimitReached)
    else None

object SubmitPlayerTextAPIMessage:
  given Codec.AsObject[SubmitPlayerTextAPIMessage] = deriveCodec
