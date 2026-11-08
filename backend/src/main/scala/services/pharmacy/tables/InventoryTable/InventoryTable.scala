// 库存批次表访问层：只负责 medicine_inventory 表，不管理药品目录。
package services.pharmacy.tables

import cats.effect.IO
import cats.syntax.all.*
import services.pharmacy.objects.{Inventory, InventoryItem, Medicine, MedicineLevel, MedicinePrice}
import services.system.HttpError
import services.system.database.JdbcSupport

import java.sql.Connection

object InventoryTable:
  private val createSql = "insert into medicine_inventory (id, game_id, medicine_id, quantity, expiry_date) values (?, ?, ?, ?, ?)"

  def create(connection: Connection, gameId: String, item: InventoryItem): IO[Unit] =
    JdbcSupport.update(connection, createSql) { statement =>
      statement.setString(1, item.id)
      statement.setString(2, gameId)
      statement.setString(3, item.medicine.id)
      statement.setInt(4, item.quantity)
      statement.setObject(5, java.time.LocalDate.parse(item.expiryDate))
    }.map(_ => ())

  private val addStockSql =
    """
      |insert into medicine_inventory (id, game_id, medicine_id, quantity, expiry_date)
      |values (?, ?, ?, ?, ?)
      |on conflict (game_id, medicine_id, expiry_date)
      |do update set quantity = medicine_inventory.quantity + excluded.quantity
      |""".stripMargin

  def addStock(connection: Connection, gameId: String, medicine: Medicine, quantity: Int, expiryDate: String): IO[Unit] =
    if quantity <= 0 then IO.raiseError(HttpError.BadRequest("采购数量必须大于 0。"))
    else
      JdbcSupport.update(connection, addStockSql) { statement =>
        statement.setString(1, s"inventory-${java.util.UUID.randomUUID()}")
        statement.setString(2, gameId)
        statement.setString(3, medicine.id)
        statement.setInt(4, quantity)
        statement.setObject(5, java.time.LocalDate.parse(expiryDate))
      }.map(_ => ())

  private val selectSql =
    """
      |select i.id, i.quantity, i.expiry_date, m.id as medicine_id, m.name,
      |       p.purchase_price, p.sale_price, m.level
      |from medicine_inventory i
      |join medicines m on m.id = i.medicine_id
      |join medicine_prices p on p.medicine_id = m.id
      |""".stripMargin

  private val findByIdSql = selectSql + "where i.id = ?"

  def findById(connection: Connection, inventoryItemId: String): IO[Option[InventoryItem]] =
    JdbcSupport.queryOne(connection, findByIdSql)(_.setString(1, inventoryItemId))(readInventoryItem)

  private val findByGameIdSql = selectSql + "where i.game_id = ? order by i.id"

  def findByGameId(connection: Connection, gameId: String): IO[List[InventoryItem]] =
    JdbcSupport.query(connection, findByGameIdSql)(_.setString(1, gameId))(readInventoryItem)

  private val updateSql = "update medicine_inventory set quantity = ?, expiry_date = ? where id = ?"

  def update(connection: Connection, item: InventoryItem): IO[Unit] =
    JdbcSupport.updateExactlyOne(
      connection,
      updateSql,
      s"未找到库存批次：${item.id}"
    ) { statement =>
      statement.setInt(1, item.quantity)
      statement.setObject(2, java.time.LocalDate.parse(item.expiryDate))
      statement.setString(3, item.id)
    }

  private val deleteSql = "delete from medicine_inventory where id = ?"

  def delete(connection: Connection, inventoryItemId: String): IO[Unit] =
    JdbcSupport.deleteExactlyOne(connection, deleteSql, s"未找到库存批次：$inventoryItemId")(_.setString(1, inventoryItemId))

  def findAggregateByGameId(connection: Connection, gameId: String): IO[Inventory] =
    findByGameId(connection, gameId).map(items => Inventory(items))

  def consumeMedicine(connection: Connection, gameId: String, medicineId: String, quantity: Int): IO[Unit] =
    if quantity <= 0 then IO.raiseError(HttpError.BadRequest("消耗数量必须大于 0。"))
    else
      findByGameId(connection, gameId).map(_.filter(_.medicine.id == medicineId).sortBy(_.expiryDate)).flatMap { lots =>
        val available = lots.map(_.quantity).sum
        if available < quantity then IO.raiseError(HttpError.Conflict(s"药品库存不足：$medicineId"))
        else
          lots.foldLeft(IO.pure(quantity)) { (remainingProgram, lot) =>
            remainingProgram.flatMap { remaining =>
              if remaining <= 0 then IO.pure(0)
              else
                val consumed = remaining.min(lot.quantity)
                val nextQuantity = lot.quantity - consumed
                val write =
                  if nextQuantity == 0 then delete(connection, lot.id)
                  else update(connection, lot.copy(quantity = nextQuantity))
                write.as(remaining - consumed)
            }
          }.void
      }

  private def readInventoryItem(resultSet: java.sql.ResultSet): InventoryItem =
    InventoryItem(
      id = resultSet.getString("id"),
      medicine = Medicine(
        id = resultSet.getString("medicine_id"),
        name = resultSet.getString("name"),
        price = MedicinePrice(
          purchasePrice = BigDecimal(resultSet.getBigDecimal("purchase_price")),
          salePrice = BigDecimal(resultSet.getBigDecimal("sale_price"))
        ),
        level = MedicineLevel.fromString(resultSet.getString("level"))
          .fold(message => throw new IllegalStateException(message), identity)
      ),
      quantity = resultSet.getInt("quantity"),
      expiryDate = resultSet.getObject("expiry_date", classOf[java.time.LocalDate]).toString
    )

