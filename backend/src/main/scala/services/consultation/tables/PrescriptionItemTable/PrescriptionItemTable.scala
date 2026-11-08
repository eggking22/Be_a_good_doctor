// 接诊购物清单表访问层：只维护已开药的清单项，不在此阶段扣减库存或收款。
package services.consultation.tables

import cats.effect.IO
import services.consultation.objects.PrescriptionItem
import services.pharmacy.objects.{Medicine, MedicineLevel, MedicinePrice}
import services.system.database.JdbcSupport

import java.sql.Connection

object PrescriptionItemTable:
  private val addIfMissingSql =
    """
      |insert into consultation_prescription_items (consultation_id, medicine_id, quantity)
      |values (?, ?, 1)
      |on conflict (consultation_id, medicine_id) do nothing
      |""".stripMargin

  def addIfMissing(connection: Connection, consultationId: String, medicineId: String): IO[Unit] =
    JdbcSupport.update(connection, addIfMissingSql) { statement =>
      statement.setString(1, consultationId)
      statement.setString(2, medicineId)
    }.map(_ => ())

  private val findByConsultationIdSql =
    """
      |select m.id, m.name, p.purchase_price, p.sale_price, m.level, i.quantity
      |from consultation_prescription_items i
      |join medicines m on m.id = i.medicine_id
      |join medicine_prices p on p.medicine_id = m.id
      |where i.consultation_id = ?
      |order by m.id
      |""".stripMargin

  def findByConsultationId(connection: Connection, consultationId: String): IO[List[PrescriptionItem]] =
    JdbcSupport.query(connection, findByConsultationIdSql)(_.setString(1, consultationId)) { resultSet =>
      PrescriptionItem(
        medicine = Medicine(
          id = resultSet.getString("id"),
          name = resultSet.getString("name"),
          price = MedicinePrice(
            purchasePrice = BigDecimal(resultSet.getBigDecimal("purchase_price")),
            salePrice = BigDecimal(resultSet.getBigDecimal("sale_price"))
          ),
          level = MedicineLevel.fromString(resultSet.getString("level"))
            .fold(message => throw new IllegalStateException(message), identity)
        ),
        quantity = resultSet.getInt("quantity")
      )
    }
