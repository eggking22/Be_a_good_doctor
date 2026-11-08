// 新存档的药房起始库存：感冒药固定十份，并为其他高概率一级疾病药品各准备两份。
package services.pharmacy.utils

import cats.effect.IO
import cats.syntax.all.*
import services.pharmacy.tables.{DiseaseMedicineTable, DiseaseTable, InventoryTable, MedicineTable}
import services.system.HttpError

import java.sql.Connection

object InitialInventory:
  private val coldMedicineId = "medicine-cold"
  private val starterExpiryDate = "2099-12-31"

  def seed(connection: Connection, gameId: String): IO[Unit] =
    for
      coldMedicine <- MedicineTable.findById(connection, coldMedicineId).flatMap {
        case Some(value) => IO.pure(value)
        case None => IO.raiseError(HttpError.Conflict("医疗目录缺少感冒药，无法初始化药房。"))
      }
      _ <- InventoryTable.addStock(connection, gameId, coldMedicine, quantity = 10, expiryDate = starterExpiryDate)
      levelOneDiseases <- DiseaseTable.findByDifficulty(connection, difficulty = 1)
      highProbabilityMedicines <- levelOneDiseases
        .filter(_.attributes.extractionProbability >= 0.5)
        .traverse(disease => DiseaseMedicineTable.findMedicinesByDiseaseId(connection, disease.id))
        .map(_.flatten.groupBy(_.id).values.map(_.head).toList)
      _ <- highProbabilityMedicines.filterNot(_.id == coldMedicine.id).traverse { medicine =>
        InventoryTable.addStock(connection, gameId, medicine, quantity = 2, expiryDate = starterExpiryDate)
      }
    yield ()
