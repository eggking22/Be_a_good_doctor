// 数据库初始化编排：按外键依赖顺序调用各表所属模块的初始化器。
package services.system.database

import cats.effect.IO
import cats.syntax.all.*
import services.clinic.tables.{FacilityTableInitializer, StaffTableInitializer}
import services.consultation.tables.{CaseSymptomTableInitializer, ConsultationTableInitializer, DialogueMessageTableInitializer, PatientCaseTableInitializer, PlayerTextAnalysisTableInitializer, PrescriptionItemTableInitializer}
import services.economy.tables.{DailyStatementTableInitializer, LedgerTableInitializer}
import services.event.tables.EventTableInitializer
import services.game.tables.GameStateTableInitializer
import services.patient.tables.PatientTableInitializer
import services.pharmacy.api.InitializeMedicalCatalogAPIMessage
import services.pharmacy.tables.{DiseaseMedicineTableInitializer, DiseaseSymptomTableInitializer, DiseaseTableInitializer, InventoryTableInitializer, MedicinePriceTableInitializer, MedicineTableInitializer, SymptomTableInitializer}
import services.reputation.tables.ReputationTableInitializer

import java.sql.Connection

object DatabaseSchemaInitializer:
  def initialize(connection: Connection): IO[Unit] =
    List(
      GameStateTableInitializer.initialize(connection),
      LedgerTableInitializer.initialize(connection),
      MedicineTableInitializer.initialize(connection),
      MedicinePriceTableInitializer.initialize(connection),
      SymptomTableInitializer.initialize(connection),
      DiseaseTableInitializer.initialize(connection),
      DiseaseSymptomTableInitializer.initialize(connection),
      DiseaseMedicineTableInitializer.initialize(connection),
      InventoryTableInitializer.initialize(connection),
      PatientTableInitializer.initialize(connection),
      PatientCaseTableInitializer.initialize(connection),
      CaseSymptomTableInitializer.initialize(connection),
      ConsultationTableInitializer.initialize(connection),
      PrescriptionItemTableInitializer.initialize(connection),
      DialogueMessageTableInitializer.initialize(connection),
      PlayerTextAnalysisTableInitializer.initialize(connection),
      StaffTableInitializer.initialize(connection),
      FacilityTableInitializer.initialize(connection),
      ReputationTableInitializer.initialize(connection),
      EventTableInitializer.initialize(connection),
      DailyStatementTableInitializer.initialize(connection),
      InitializeMedicalCatalogAPIMessage().plan(connection)
    ).sequence_.void
