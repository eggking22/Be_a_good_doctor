// 声誉流程编排：统一更新当前分数与历史记录，并保留特殊事件入口。
package services.reputation.utils

import cats.effect.IO
import cats.syntax.all.*
import services.consultation.objects.PlayerAttitude
import services.consultation.tables.{ConsultationTable, PatientCaseTable}
import services.game.objects.GameSessionRecord
import services.game.tables.GameStateTable
import services.reputation.calculators.ReputationEvaluator
import services.reputation.objects.{RecoveryFollowUp, ReputationChange}
import services.reputation.tables.ReputationTable
import services.system.HttpError

import java.sql.Connection
import java.time.OffsetDateTime
import scala.util.Random

object ReputationOperations:
  private val followUpCheckProbability = 0.10

  def departureDelta(hasPrescription: Boolean, attitude: Option[PlayerAttitude]): (Double, String) =
    if !hasPrescription then (-1.0, "未开药结束接诊")
    else attitude match
      case Some(PlayerAttitude.Polite) => (0.5, "患者评价：沟通态度优秀")
      case Some(PlayerAttitude.Neutral) => (0.0, "患者评价：沟通态度中性")
      case Some(PlayerAttitude.Rude | PlayerAttitude.Abusive) => (-0.5, "患者评价：沟通态度恶劣")
      case None => (0.0, "患者评价：未形成有效态度记录")

  def applySpecialEvent(connection: Connection, gameId: String, delta: Double, reason: String): IO[GameSessionRecord] =
    if reason.trim.isEmpty then IO.raiseError(HttpError.BadRequest("特殊事件的声誉原因不能为空。"))
    else
      GameStateTable.findById(connection, gameId.trim).flatMap {
        case Some(game) => applyDelta(connection, game, delta, s"特殊事件：${reason.trim}")
        case None => IO.raiseError(HttpError.NotFound(s"未找到游戏：${gameId.trim}"))
      }

  def tryRecoveryFollowUp(connection: Connection, gameId: String): IO[Option[RecoveryFollowUp]] =
    tryRecoveryFollowUp(connection, gameId, new Random())

  private[reputation] def tryRecoveryFollowUp(connection: Connection, gameId: String, random: Random): IO[Option[RecoveryFollowUp]] =
    if random.nextDouble() >= followUpCheckProbability then IO.pure(None)
    else
      GameStateTable.findById(connection, gameId.trim).flatMap {
        case None => IO.raiseError(HttpError.NotFound(s"未找到游戏：${gameId.trim}"))
        case Some(game) =>
          val earliestDay = (game.currentDay - 7).max(1)
          ConsultationTable.findEligibleForFollowUp(connection, game.id, earliestDay, game.currentDay).flatMap { candidates =>
            candidates match
              case Nil => IO.pure(None)
              case _ =>
                val consultation = candidates(random.nextInt(candidates.size))
                for
                  patientCase <- consultation.caseId.traverse(PatientCaseTable.findById(connection, _)).map(_.flatten)
                  daysAgo = game.currentDay - consultation.closedDay.getOrElse(game.currentDay)
                  message = recoveryMessage(patientCase.map(_.disease.name), daysAgo)
                  _ <- applyDelta(connection, game, 1.0, s"患者康复回访：${consultation.patient.name}")
                  _ <- ConsultationTable.markFollowUpCompleted(connection, consultation.id)
                yield Some(RecoveryFollowUp(consultation.patient.name, message, reputationDelta = 1.0))
          }
      }

  def applyDelta(connection: Connection, game: GameSessionRecord, delta: Double, reason: String): IO[GameSessionRecord] =
    val updated = game.copy(
      reputationScore = ReputationEvaluator.applyDelta(game.reputationScore, delta),
      updatedAt = OffsetDateTime.now()
    )
    GameStateTable.update(connection, updated) *>
      ReputationTable.appendChange(connection, game.id, ReputationChange(delta, reason, updated.updatedAt.toString)).as(updated)

  private def recoveryMessage(diseaseName: Option[String], daysAgo: Int): String =
    val condition = diseaseName.fold("病")(name => s"$name")
    if daysAgo <= 2 then s"医生，我前两天的${condition}已经好多了，按您开的药吃后恢复得不错，谢谢您！"
    else s"医生，我前些天的${condition}已经好多了，按您开的药吃后恢复得不错，谢谢您！"
