// 疾病—药物关系表访问层：支持增加、移除及双向查询。
package services.pharmacy.tables

import cats.effect.IO
import services.pharmacy.objects.{CatalogDiseaseMedicine, Disease, DiseaseAttributes, Medicine, MedicineLevel, MedicinePrice}
import services.system.database.JdbcSupport

import java.sql.Connection

object DiseaseMedicineTable:
  private val linkSql =
    "insert into disease_medicines (disease_id, medicine_id) values (?, ?) on conflict (disease_id, medicine_id) do nothing"

  def link(connection: Connection, diseaseId: String, medicineId: String): IO[Unit] =
    JdbcSupport.update(connection, linkSql) { statement =>
      statement.setString(1, diseaseId)
      statement.setString(2, medicineId)
    }.map(_ => ())

  private val unlinkSql = "delete from disease_medicines where disease_id = ? and medicine_id = ?"

  def unlink(connection: Connection, diseaseId: String, medicineId: String): IO[Unit] =
    JdbcSupport.deleteExactlyOne(connection, unlinkSql, s"未找到疾病药物关联：$diseaseId / $medicineId") { statement =>
      statement.setString(1, diseaseId)
      statement.setString(2, medicineId)
    }

  private val findMedicinesByDiseaseIdSql =
    """
      |select m.id, m.name, m.level, p.purchase_price, p.sale_price
      |from medicines m
      |join medicine_prices p on p.medicine_id = m.id
      |join disease_medicines dm on dm.medicine_id = m.id
      |where dm.disease_id = ? order by m.id
      |""".stripMargin

  def findMedicinesByDiseaseId(connection: Connection, diseaseId: String): IO[List[Medicine]] =
    JdbcSupport.query(connection, findMedicinesByDiseaseIdSql)(_.setString(1, diseaseId)) { row =>
      Medicine(
        id = row.getString("id"),
        name = row.getString("name"),
        price = MedicinePrice(
          purchasePrice = BigDecimal(row.getBigDecimal("purchase_price")),
          salePrice = BigDecimal(row.getBigDecimal("sale_price"))
        ),
        level = MedicineLevel.fromString(row.getString("level"))
          .fold(message => throw new IllegalStateException(message), identity)
      )
    }

  private val findAllSql = "select disease_id, medicine_id from disease_medicines order by disease_id, medicine_id"

  def findAll(connection: Connection): IO[List[CatalogDiseaseMedicine]] =
    JdbcSupport.query(connection, findAllSql)(_ => ()) { row =>
      CatalogDiseaseMedicine(row.getString("disease_id"), row.getString("medicine_id"))
    }

  private val findDiseasesByMedicineIdSql =
    """
      |select d.id, d.name, d.difficulty, d.extraction_probability
      |from diseases d join disease_medicines dm on dm.disease_id = d.id
      |where dm.medicine_id = ? order by d.id
      |""".stripMargin

  def findDiseasesByMedicineId(connection: Connection, medicineId: String): IO[List[Disease]] =
    JdbcSupport.query(connection, findDiseasesByMedicineIdSql)(_.setString(1, medicineId)) { row =>
      Disease(
        row.getString("id"),
        row.getString("name"),
        DiseaseAttributes(row.getInt("difficulty"), row.getDouble("extraction_probability"))
      )
    }
