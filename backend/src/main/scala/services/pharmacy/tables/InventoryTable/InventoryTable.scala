// 库存批次表访问层：只负责 medicine_inventory 表，不管理药品目录。
package services.pharmacy.tables

import cats.effect.IO
import services.pharmacy.objects.{Inventory, InventoryItem, Medicine, MedicineLevel, MedicinePrice}
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

