// 药房流程编排：市场采购会即时更新现金、库存和现金流水。
package services.pharmacy.utils

import cats.effect.IO
import services.economy.objects.{LedgerTransaction, LedgerTransactionType}
import services.economy.tables.LedgerTable
import services.economy.utils.Money
import services.game.calculators.GameClock
import services.game.tables.GameStateTable
import services.pharmacy.objects.PharmacyOverview
import services.pharmacy.tables.{InventoryTable, MedicineTable}
import services.system.HttpError

import java.sql.Connection
import java.time.OffsetDateTime
import java.util.UUID

object PharmacyOperations:
  private val purchasedMedicineExpiryDate = "2099-12-31"

  def overview(connection: Connection, gameId: String): IO[PharmacyOverview] =
    val normalizedGameId = gameId.trim
    GameStateTable.findById(connection, normalizedGameId).flatMap {
      case Some(game) => buildOverview(connection, normalizedGameId, game.cash, game.currentMinute)
      case None => IO.raiseError(HttpError.NotFound(s"未找到游戏：$normalizedGameId"))
    }

  def purchase(connection: Connection, gameId: String, medicineId: String, quantity: Int): IO[PharmacyOverview] =
    val normalizedGameId = gameId.trim
    val normalizedMedicineId = medicineId.trim
    if normalizedGameId.isEmpty || normalizedMedicineId.isEmpty then
      IO.raiseError(HttpError.BadRequest("游戏 ID 和药品 ID 不能为空。"))
    else if quantity <= 0 || quantity > 99 then
      IO.raiseError(HttpError.BadRequest("单次采购数量必须在 1 到 99 之间。"))
    else
      for
        game <- GameStateTable.findById(connection, normalizedGameId).flatMap {
          case Some(value) => IO.pure(value)
          case None => IO.raiseError(HttpError.NotFound(s"未找到游戏：$normalizedGameId"))
        }
        _ <- if game.currentMinute == GameClock.preparationStartMinute then IO.unit
        else IO.raiseError(HttpError.Conflict("药房采购只在每日 08:30 的准备阶段开放。"))
        medicine <- MedicineTable.findById(connection, normalizedMedicineId).flatMap {
          case Some(value) => IO.pure(value)
          case None => IO.raiseError(HttpError.NotFound(s"未找到药品：$normalizedMedicineId"))
        }
        cost = Money.round(medicine.price.purchasePrice * quantity)
        _ <- if game.cash >= cost then IO.unit
        else IO.raiseError(HttpError.Conflict("现金不足，无法完成采购。"))
        _ <- InventoryTable.addStock(connection, normalizedGameId, medicine, quantity, purchasedMedicineExpiryDate)
        updatedGame = game.copy(cash = Money.round(game.cash - cost), updatedAt = OffsetDateTime.now())
        _ <- GameStateTable.update(connection, updatedGame)
        _ <- LedgerTable.append(
          connection,
          LedgerTransaction(
            id = s"ledger-${UUID.randomUUID()}",
            gameId = normalizedGameId,
            transactionType = LedgerTransactionType.MedicinePurchase,
            amount = cost,
            occurredOn = game.currentDay,
            description = s"市场采购：${medicine.name} × $quantity"
          )
        )
        result <- buildOverview(connection, normalizedGameId, updatedGame.cash, updatedGame.currentMinute)
      yield result

  private def buildOverview(connection: Connection, gameId: String, cash: BigDecimal, currentMinute: Int): IO[PharmacyOverview] =
    for
      inventory <- InventoryTable.findAggregateByGameId(connection, gameId)
      medicines <- MedicineTable.findAll(connection)
    yield PharmacyOverview(
      gameId = gameId,
      cash = cash,
      currentMinute = currentMinute,
      canPurchase = currentMinute == GameClock.preparationStartMinute,
      inventory = inventory,
      marketMedicines = medicines
    )
