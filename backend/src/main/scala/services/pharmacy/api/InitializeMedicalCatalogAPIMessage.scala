// 初始化游戏示例目录：缺失时插入，重复执行不覆盖已有数据。
package services.pharmacy.api

import cats.effect.IO
import cats.syntax.all.*
import services.pharmacy.tables.{DiseaseMedicineTable, DiseaseSymptomTable, DiseaseTable, MedicineTable, SymptomTable}
import services.pharmacy.utils.InitialMedicalCatalog
import services.system.api.APIMessage

import java.sql.Connection

// 此消息仅供后端启动时调用，不向前端注册。
final case class InitializeMedicalCatalogAPIMessage() extends APIMessage[Unit]:
  override def plan(connection: Connection): IO[Unit] =
    for
      _ <- InitialMedicalCatalog.symptoms.traverse_(SymptomTable.createIfMissing(connection, _))
      _ <- InitialMedicalCatalog.diseases.traverse_(DiseaseTable.createIfMissing(connection, _))
      _ <- InitialMedicalCatalog.medicines.traverse_(MedicineTable.createIfMissing(connection, _))
      _ <- InitialMedicalCatalog.diseaseSymptoms.traverse_ { case (diseaseId, symptomId) =>
        DiseaseSymptomTable.link(connection, diseaseId, symptomId)
      }
      _ <- InitialMedicalCatalog.diseaseMedicines.traverse_ { case (diseaseId, medicineId) =>
        DiseaseMedicineTable.link(connection, diseaseId, medicineId)
      }
    yield ()
