// 药品目录表访问层：只维护药品名称和类别；价格由 MedicinePriceTable 单独维护。
package services.pharmacy.tables

import cats.effect.IO
import services.pharmacy.objects.{Medicine, MedicineLevel, MedicinePrice}
import services.system.database.JdbcSupport

import java.sql.Connection

object MedicineTable:
  private val createSql = "insert into medicines (id, name, level) values (?, ?, ?)"

  def create(connection: Connection, medicine: Medicine): IO[Unit] =
    for
      _ <- JdbcSupport.update(connection, createSql) { statement =>
        statement.setString(1, medicine.id)
        statement.setString(2, medicine.name)
        statement.setString(3, medicine.level.toString)
      }
      _ <- MedicinePriceTable.create(connection, medicine.id, medicine.price)
    yield ()

  private val createIfMissingSql =
    "insert into medicines (id, name, level) values (?, ?, ?) on conflict (id) do nothing"

  // 只补齐缺失的示例药品和价格，不覆盖已调整的药品基础信息或价格。
  def createIfMissing(connection: Connection, medicine: Medicine): IO[Unit] =
    for
      _ <- JdbcSupport.update(connection, createIfMissingSql) { statement =>
        statement.setString(1, medicine.id)
        statement.setString(2, medicine.name)
        statement.setString(3, medicine.level.toString)
      }
      _ <- MedicinePriceTable.createIfMissing(connection, medicine.id, medicine.price)
    yield ()

  private val findByIdSql =
    """
      |select m.id, m.name, m.level, p.purchase_price, p.sale_price
      |from medicines m join medicine_prices p on p.medicine_id = m.id
      |where m.id = ?
      |""".stripMargin

  def findById(connection: Connection, medicineId: String): IO[Option[Medicine]] =
    JdbcSupport.queryOne(
      connection,
      findByIdSql
    )(_.setString(1, medicineId))(readMedicine)

  private val findAllSql =
    """
      |select m.id, m.name, m.level, p.purchase_price, p.sale_price
      |from medicines m join medicine_prices p on p.medicine_id = m.id
      |order by m.id
      |""".stripMargin

  def findAll(connection: Connection): IO[List[Medicine]] =
    JdbcSupport.query(
      connection,
      findAllSql
    )(statement => ())(readMedicine)

  private val updateSql = "update medicines set name = ?, level = ? where id = ?"

  def update(connection: Connection, medicine: Medicine): IO[Unit] =
    for
      _ <- JdbcSupport.updateExactlyOne(
        connection,
        updateSql,
        s"未找到药品：${medicine.id}"
      ) { statement =>
        statement.setString(1, medicine.name)
        statement.setString(2, medicine.level.toString)
        statement.setString(3, medicine.id)
      }
      _ <- MedicinePriceTable.update(connection, medicine.id, medicine.price)
    yield ()

  private val deleteSql = "delete from medicines where id = ?"

  def delete(connection: Connection, medicineId: String): IO[Unit] =
    JdbcSupport.deleteExactlyOne(connection, deleteSql, s"未找到药品：$medicineId")(_.setString(1, medicineId))

  private def readMedicine(resultSet: java.sql.ResultSet): Medicine =
    Medicine(
      id = resultSet.getString("id"),
      name = resultSet.getString("name"),
      price = MedicinePrice(
        purchasePrice = BigDecimal(resultSet.getBigDecimal("purchase_price")),
        salePrice = BigDecimal(resultSet.getBigDecimal("sale_price"))
      ),
      level = MedicineLevel.fromString(resultSet.getString("level"))
        .fold(message => throw new IllegalStateException(message), identity)
    )
