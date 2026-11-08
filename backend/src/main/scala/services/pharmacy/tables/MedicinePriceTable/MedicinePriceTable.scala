// 药品价格表访问层：只负责 medicine_prices，不维护药品名称、类别或疾病关系。
package services.pharmacy.tables

import cats.effect.IO
import services.pharmacy.objects.MedicinePrice
import services.system.database.JdbcSupport

import java.sql.Connection

object MedicinePriceTable:
  private val createSql =
    "insert into medicine_prices (medicine_id, purchase_price, sale_price) values (?, ?, ?)"

  def create(connection: Connection, medicineId: String, price: MedicinePrice): IO[Unit] =
    JdbcSupport.update(connection, createSql) { statement =>
      statement.setString(1, medicineId)
      statement.setBigDecimal(2, price.purchasePrice.bigDecimal)
      statement.setBigDecimal(3, price.salePrice.bigDecimal)
    }.map(_ => ())

  private val createIfMissingSql =
    "insert into medicine_prices (medicine_id, purchase_price, sale_price) values (?, ?, ?) on conflict (medicine_id) do nothing"

  def createIfMissing(connection: Connection, medicineId: String, price: MedicinePrice): IO[Unit] =
    JdbcSupport.update(connection, createIfMissingSql) { statement =>
      statement.setString(1, medicineId)
      statement.setBigDecimal(2, price.purchasePrice.bigDecimal)
      statement.setBigDecimal(3, price.salePrice.bigDecimal)
    }.map(_ => ())

  private val updateSql =
    "update medicine_prices set purchase_price = ?, sale_price = ? where medicine_id = ?"

  def update(connection: Connection, medicineId: String, price: MedicinePrice): IO[Unit] =
    JdbcSupport.updateExactlyOne(
      connection,
      updateSql,
      s"未找到药品价格：$medicineId"
    ) { statement =>
      statement.setBigDecimal(1, price.purchasePrice.bigDecimal)
      statement.setBigDecimal(2, price.salePrice.bigDecimal)
      statement.setString(3, medicineId)
    }

  private val findByMedicineIdSql =
    "select purchase_price, sale_price from medicine_prices where medicine_id = ?"

  def findByMedicineId(connection: Connection, medicineId: String): IO[Option[MedicinePrice]] =
    JdbcSupport.queryOne(connection, findByMedicineIdSql)(_.setString(1, medicineId)) { resultSet =>
      MedicinePrice(
        purchasePrice = BigDecimal(resultSet.getBigDecimal("purchase_price")),
        salePrice = BigDecimal(resultSet.getBigDecimal("sale_price"))
      )
    }

  private val deleteSql = "delete from medicine_prices where medicine_id = ?"

  def delete(connection: Connection, medicineId: String): IO[Unit] =
    JdbcSupport.deleteExactlyOne(connection, deleteSql, s"未找到药品价格：$medicineId")(_.setString(1, medicineId))
