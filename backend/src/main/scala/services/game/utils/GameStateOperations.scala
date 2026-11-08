// 游戏服务内部操作：组合 API 需要的业务步骤，但不作为独立 Service 层暴露。
package services.game.utils

import cats.effect.IO
import cats.syntax.all.*
import services.clinic.objects.Clinic
import services.clinic.tables.{FacilityTable, StaffTable}
import services.consultation.objects.{ConsultationSnapshot, ConsultationStatus}
import services.consultation.tables.{ConsultationTable, PatientCaseTable, PlayerTextAnalysisTable, PrescriptionItemTable}
import services.economy.objects.CashFlow
import services.economy.tables.{DailyStatementTable, LedgerTable}
import services.event.tables.EventTable
import services.game.objects.{GameSaveSummary, GameSessionRecord, GameState, LoadedGameState}
import services.game.calculators.{GameClock, GameDayCalculator}
import services.game.calculators.PatientArrivalCalculator
import services.game.tables.GameStateTable
import services.game.utils.GameIdGenerator
import services.patient.api.GeneratePatientAPIMessage
import services.patient.tables.PatientTable
import services.pharmacy.tables.InventoryTable
import services.reputation.tables.ReputationTable
import services.system.HttpError

import java.sql.Connection
import java.time.{Instant, OffsetDateTime}

object GameStateOperations:
  private val startingCash = BigDecimal(10000)
  private val startingReputation = 60.0

  private[game] def start(connection: Connection, playerName: String, replaceGameId: Option[String] = None): IO[GameState] =
    val normalizedName = playerName.trim
    if normalizedName.isEmpty then IO.raiseError(HttpError.BadRequest("玩家名称不能为空。"))
    else
      val state = initialState(normalizedName, Instant.now())
      val normalizedReplacement = replaceGameId.map(_.trim).filter(_.nonEmpty)
      normalizedReplacement.fold(IO.unit)(gameId => GameStateTable.deleteIfExists(connection, gameId)) *>
        GameStateTable.insert(connection, toSessionRecord(state)).flatMap { _ =>
          generatePatients(connection, state.id, PatientArrivalCalculator.initialPatientCount).map(_ => state)
        }

  private[game] def get(connection: Connection, gameId: String): IO[GameState] =
    loadComplete(connection, gameId).map(_.state)

  private[game] def loadComplete(connection: Connection, gameId: String): IO[LoadedGameState] =
    val normalizedGameId = gameId.trim
    GameStateTable.findById(connection, normalizedGameId).flatMap {
      case Some(record) =>
        for
          employees <- StaffTable.findByGameId(connection, normalizedGameId)
          facilities <- FacilityTable.findByGameId(connection, normalizedGameId)
          clinic = Clinic(facilities = facilities, employees = employees)
          inventory <- InventoryTable.findAggregateByGameId(connection, normalizedGameId)
          patients <- PatientTable.findByGameId(connection, normalizedGameId)
          patientCases <- PatientCaseTable.findByGameId(connection, normalizedGameId)
          consultations <- ConsultationTable.findByGameId(connection, normalizedGameId)
          consultationSnapshots <- consultations.traverse { consultation =>
            for
              analyses <- PlayerTextAnalysisTable.findByConsultationId(connection, consultation.id)
              prescriptions <- PrescriptionItemTable.findByConsultationId(connection, consultation.id)
            yield ConsultationSnapshot(
              consultation = consultation,
              patientCase = consultation.caseId.flatMap(caseId => patientCases.find(_.id == caseId)),
              playerTextAnalyses = analyses,
              prescriptionItems = prescriptions
            )
          }
          events <- EventTable.findByGameId(connection, normalizedGameId)
          reputationHistory <- ReputationTable.findChangesByGameId(connection, normalizedGameId)
          dailyStatements <- DailyStatementTable.findByGameId(connection, normalizedGameId)
          ledgerTransactions <- LedgerTable.findByGameId(connection, normalizedGameId)
          baseState = fromSessionRecord(record, clinic, inventory)
          state = baseState.copy(
            finance = baseState.finance.copy(
              cashFlow = dailyStatements.lastOption.map(item => CashFlow(item.statement.income, item.statement.expense)),
              dailyStatement = dailyStatements.lastOption.map(_.statement)
            )
          )
        yield LoadedGameState(
          state = state,
          patients = patients,
          patientCases = patientCases,
          consultations = consultations,
          consultationSnapshots = consultationSnapshots,
          events = events,
          reputationHistory = reputationHistory,
          dailyStatements = dailyStatements,
          ledgerTransactions = ledgerTransactions
        )
      case None => IO.raiseError(HttpError.NotFound(s"未找到游戏：${gameId.trim}"))
    }

  private[game] def delete(connection: Connection, gameId: String): IO[Unit] =
    GameStateTable.delete(connection, gameId.trim)

  private[game] def list(connection: Connection): IO[List[GameSaveSummary]] =
    GameStateTable.findAll(connection).map(_.map(GameSaveSummary.fromRecord))

  private[game] def openBusinessDay(connection: Connection, gameId: String): IO[GameState] =
    get(connection, gameId).flatMap {
      case state if state.bankrupt => IO.raiseError(HttpError.Conflict("游戏已经破产，不能开始营业。"))
      case state if GameClock.hasBusinessEnded(state.currentMinute) =>
        IO.raiseError(HttpError.Conflict("今日营业已经结束，请先进行日结算。"))
      case state =>
        val openedState = state.copy(
          currentMinute = GameClock.openingMinute(state.currentMinute),
          updatedAt = Instant.now().toString
        )
        GameStateTable.update(connection, toSessionRecord(openedState)).as(openedState)
    }

  private[game] def advance(connection: Connection, gameId: String): IO[GameState] =
    get(connection, gameId).flatMap {
      case state if state.bankrupt =>
        IO.raiseError(HttpError.Conflict("游戏已经破产，不能继续推进经营日。"))
      case state if !GameClock.hasBusinessEnded(state.currentMinute) =>
        IO.raiseError(HttpError.Conflict("请在 17:00 营业结束后再进行日结算。"))
      case state =>
        ConsultationTable.findByGameId(connection, state.id).flatMap { consultations =>
          if consultations.exists(_.status == ConsultationStatus.InProgress) then
            IO.raiseError(HttpError.Conflict("仍有进行中的接诊，请先结束当前问诊。"))
          else
            val nextState = GameDayCalculator.advanceOneDay(state)
            GameStateTable.update(connection, toSessionRecord(nextState)).flatMap { _ =>
              generatePatients(connection, nextState.id, PatientArrivalCalculator.dailyPatientCount(nextState.reputation.score)).map(_ => nextState)
            }
        }
    }

  private[game] def generatePatients(connection: Connection, gameId: String, count: Int): IO[Unit] =
    List.fill(count.max(0))(()).foldLeft(IO.unit) { (program, _) =>
      program.flatMap(_ => GeneratePatientAPIMessage(gameId).plan(connection).map(_ => ()))
    }

  private[game] def initialState(playerName: String, now: Instant): GameState =
    GameState(
      id = GameIdGenerator.nextId(),
      playerName = playerName,
      currentDay = 1,
      currentMinute = GameClock.preparationStartMinute,
      finance = services.economy.objects.FinanceState(startingCash, None, None),
      reputation = services.reputation.objects.Reputation(startingReputation),
      clinic = Clinic.empty,
      inventory = services.pharmacy.objects.Inventory.empty,
      bankrupt = false,
      createdAt = now.toString,
      updatedAt = now.toString
    )

  private[game] def toSessionRecord(state: GameState): GameSessionRecord =
    GameSessionRecord(
      id = state.id,
      playerName = state.playerName,
      currentDay = state.currentDay,
      currentMinute = state.currentMinute,
      cash = state.finance.cash,
      reputationScore = state.reputation.score,
      bankrupt = state.bankrupt,
      createdAt = OffsetDateTime.parse(state.createdAt),
      updatedAt = OffsetDateTime.parse(state.updatedAt)
    )

  private[game] def fromSessionRecord(record: GameSessionRecord, clinic: Clinic, inventory: services.pharmacy.objects.Inventory): GameState =
    GameState(
      id = record.id,
      playerName = record.playerName,
      currentDay = record.currentDay,
      currentMinute = record.currentMinute,
      finance = services.economy.objects.FinanceState(record.cash, None, None),
      reputation = services.reputation.objects.Reputation(record.reputationScore),
      clinic = clinic,
      inventory = inventory,
      bankrupt = record.bankrupt,
      createdAt = record.createdAt.toString,
      updatedAt = record.updatedAt.toString
    )
