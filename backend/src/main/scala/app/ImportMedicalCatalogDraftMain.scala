// 目录维护命令入口：显式导入已审核 JSON，不在应用的每次启动中自动覆盖目录或价格。
package app

import cats.effect.{IO, IOApp}
import services.pharmacy.api.ImportMedicalCatalogDraftAPIMessage
import services.system.database.{DatabaseSchemaInitializer, DatabaseSession}

object ImportMedicalCatalogDraftMain extends IOApp.Simple:
  override def run: IO[Unit] =
    DatabaseSession.initialize.use { databaseSession =>
      databaseSession.withTransactionConnection { connection =>
        DatabaseSchemaInitializer.initialize(connection) *>
          ImportMedicalCatalogDraftAPIMessage().plan(connection)
      }
    }.flatMap(result => IO.println(s"医疗目录导入完成：疾病 ${result.diseaseCount}，症状 ${result.symptomCount}，药物 ${result.medicineCount}。"))
