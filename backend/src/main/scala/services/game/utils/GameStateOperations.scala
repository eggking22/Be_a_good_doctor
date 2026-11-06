// 游戏服务内部操作：组合 API 需要的业务步骤，但不作为独立 Service 层暴露。
package services.game.utils

import cats.effect.IO
import services.clinic.objects.Clinic
import services.clinic.tables.{FacilityTable, StaffTable}
import services.consultation.tables.ConsultationTable
import services.economy.objects.CashFlow
import services.economy.tables.{DailyStatementTable, LedgerTable}
import services.event.tables.EventTable
import services.game.objects.{GameSessionRecord, GameState, LoadedGameState}
import services.game.calculators.GameDayCalculator
import services.game.tables.GameStateTable
import services.game.utils.GameIdGenerator
import services.patient.tables.PatientTable
import services.pharmacy.tables.InventoryTable
import services.reputation.tables.ReputationTable
import services.system.HttpError

import java.sql.Connection
import java.time.{Instant, OffsetDateTime}

object GameStateOperations:
  private val startingCash = BigDecimal(10000)
  private val startingReputation = 60.0

  private[game] def start(connection: Connection, playerName: String): IO[GameState] =
    val normalizedName = playerName.trim
    if normalizedName.isEmpty then IO.raiseError(HttpError.BadRequest("玩家名称不能为空。"))
    else
      val state = initialState(normalizedName, Instant.now())
      GameStateTable.insert(connection, toSessionRecord(state)).map(_ => state)

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
          consultations <- ConsultationTable.findByGameId(connection, normalizedGameId)
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
          consultations = consultations,
          events = events,
          reputationHistory = reputationHistory,
          dailyStatements = dailyStatements,
          ledgerTransactions = ledgerTransactions
        )
      case None => IO.raiseError(HttpError.NotFound(s"未找到游戏：${gameId.trim}"))
    }

  private[game] def delete(connection: Connection, gameId: String): IO[Unit] =
    GameStateTable.delete(connection, gameId.trim)

  private[game] def advance(connection: Connection, gameId: String): IO[GameState] =
    get(connection, gameId).flatMap {
      case state if state.bankrupt =>
        IO.raiseError(HttpError.Conflict("游戏已经破产，不能继续推进经营日。"))
      case state =>
        val nextState = GameDayCalculator.advanceOneDay(state)
        GameStateTable.update(connection, toSessionRecord(nextState)).map(_ => nextState)
    }

  private[game] def initialState(playerName: String, now: Instant): GameState =
    GameState(
      id = GameIdGenerator.nextId(),
      playerName = playerName,
      currentDay = 1,
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
      finance = services.economy.objects.FinanceState(record.cash, None, None),
      reputation = services.reputation.objects.Reputation(record.reputationScore),
      clinic = clinic,
      inventory = inventory,
      bankrupt = record.bankrupt,
      createdAt = record.createdAt.toString,
      updatedAt = record.updatedAt.toString
    )
