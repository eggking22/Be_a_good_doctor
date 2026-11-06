// 药品目录表访问层：药品是共享目录，不属于某一局游戏。
package services.pharmacy.tables

import cats.effect.IO
import services.pharmacy.objects.Medicine
import services.system.database.JdbcSupport

import java.sql.Connection

object MedicineTable:
  private val createSql = "insert into medicines (id, name, purchase_price, sale_price) values (?, ?, ?, ?)"

  def create(connection: Connection, medicine: Medicine): IO[Unit] =
    JdbcSupport.update(connection, createSql) { statement =>
      statement.setString(1, medicine.id)
      statement.setString(2, medicine.name)
      statement.setBigDecimal(3, medicine.purchasePrice.bigDecimal)
      statement.setBigDecimal(4, medicine.salePrice.bigDecimal)
    }.map(_ => ())

  private val findByIdSql = "select id, name, purchase_price, sale_price from medicines where id = ?"

  def findById(connection: Connection, medicineId: String): IO[Option[Medicine]] =
    JdbcSupport.queryOne(
      connection,
      findByIdSql
    )(_.setString(1, medicineId))(readMedicine)

  private val findAllSql = "select id, name, purchase_price, sale_price from medicines order by id"

  def findAll(connection: Connection): IO[List[Medicine]] =
    JdbcSupport.query(
      connection,
      findAllSql
    )(statement => ())(readMedicine)

  private val updateSql = "update medicines set name = ?, purchase_price = ?, sale_price = ? where id = ?"

  def update(connection: Connection, medicine: Medicine): IO[Unit] =
    JdbcSupport.updateExactlyOne(
      connection,
      updateSql,
      s"未找到药品：${medicine.id}"
    ) { statement =>
      statement.setString(1, medicine.name)
      statement.setBigDecimal(2, medicine.purchasePrice.bigDecimal)
      statement.setBigDecimal(3, medicine.salePrice.bigDecimal)
      statement.setString(4, medicine.id)
    }

  private val deleteSql = "delete from medicines where id = ?"

  def delete(connection: Connection, medicineId: String): IO[Unit] =
    JdbcSupport.deleteExactlyOne(connection, deleteSql, s"未找到药品：$medicineId")(_.setString(1, medicineId))

  private def readMedicine(resultSet: java.sql.ResultSet): Medicine =
    Medicine(
      id = resultSet.getString("id"),
      name = resultSet.getString("name"),
      purchasePrice = BigDecimal(resultSet.getBigDecimal("purchase_price")),
      salePrice = BigDecimal(resultSet.getBigDecimal("sale_price"))
    )

