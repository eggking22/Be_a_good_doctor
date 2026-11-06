// 阶段 2 持久化集成测试：需要连接 PostgreSQL，验证 CRUD、外键、恢复和事务回滚。
package services.game

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.all.*
import munit.CatsEffectSuite
import services.clinic.objects.{Facility, Staff, StaffRole}
import services.clinic.tables.{FacilityTable, StaffTable}
import services.consultation.objects.{Consultation, ConsultationStatus, DialogueMessage, TreatmentEvaluation}
import services.consultation.tables.{ConsultationTable, DialogueMessageTable}
import services.economy.objects.{DailyStatement, DailyStatementRecord, LedgerTransaction}
import services.economy.tables.{DailyStatementTable, LedgerTable}
import services.event.objects.{EventRiskLevel, EventType, GameEvent}
import services.event.tables.EventTable
import services.game.utils.GameStateOperations
import services.game.objects.{GameSessionRecord, GameState}
import services.game.tables.GameStateTable
import services.patient.objects.{Patient, PatientAttributes, PatientEmotion}
import services.patient.tables.PatientTable
import services.pharmacy.objects.{InventoryItem, Medicine}
import services.pharmacy.tables.{InventoryTable, MedicineTable}
import services.reputation.objects.ReputationChange
import services.reputation.tables.ReputationTable
import services.system.database.{DatabaseSchemaInitializer, DatabaseSession}

import java.time.Instant
import java.util.UUID

final class Stage2PersistenceSuite extends CatsEffectSuite:
  private def withDatabase[A](program: DatabaseSession => IO[A]): IO[A] =
    DatabaseSession.initialize.use { databaseSession =>
      databaseSession.withTransactionConnection(DatabaseSchemaInitializer.initialize) *> program(databaseSession)
    }

  private def newGame(connection: java.sql.Connection): IO[GameState] =
    GameStateOperations.start(connection, s"stage2-${UUID.randomUUID()}")

  test("所有核心表可以创建、查询、更新，并完整加载游戏数据") {
    withDatabase { databaseSession =>
      databaseSession.withTransactionConnection { connection =>
        for
          game <- newGame(connection)
          initialLoaded <- GameStateOperations.loadComplete(connection, game.id)
          _ = assertEquals(initialLoaded.state.clinic.employees, Nil)
          _ = assertEquals(initialLoaded.state.inventory.items, Nil)
          _ = assertEquals(initialLoaded.patients, Nil)
          _ = assertEquals(initialLoaded.consultations, Nil)
          staff = Staff(s"staff-${game.id}", "测试保安", StaffRole.SecurityGuard, BigDecimal(180), 20, 40, 70, true)
          facility = Facility(s"facility-${game.id}", "接待区", 1, BigDecimal(0), 0)
          medicine = Medicine(s"medicine-${game.id}", "测试药品", BigDecimal(10), BigDecimal(20))
          inventoryItem = InventoryItem(s"inventory-${game.id}", medicine, 12, "2030-01-01")
          patient = Patient(s"patient-${game.id}", "测试患者", PatientAttributes(3, 4, 2, 0.2))
          consultation = Consultation(
            id = s"consultation-${game.id}",
            patient = patient,
            emotion = PatientEmotion.Calm,
            status = ConsultationStatus.InProgress,
            dialogueHistory = Nil,
            dialogueCount = 1,
            treatmentEvaluation = Some(TreatmentEvaluation(true, 0.5, "方案匹配")),
            cashChange = BigDecimal(100),
            reputationChange = 0.5
          )
          event = GameEvent(s"event-${game.id}", EventType.MedicalDispute, "测试事件", "测试描述", EventRiskLevel.Medium, false)
          _ <- StaffTable.create(connection, game.id, staff)
          _ <- FacilityTable.create(connection, game.id, facility)
          _ <- MedicineTable.create(connection, medicine)
          _ <- InventoryTable.create(connection, game.id, inventoryItem)
          _ <- PatientTable.create(connection, game.id, patient)
          _ <- ConsultationTable.create(connection, game.id, consultation)
          _ <- DialogueMessageTable.append(connection, consultation.id, DialogueMessage("patient", "我有点不舒服", Instant.now().toString))
          _ <- EventTable.create(connection, game.id, event)
          _ <- ReputationTable.appendChange(connection, game.id, ReputationChange(0.5, "测试处理", Instant.now().toString))
          _ <- LedgerTable.append(connection, LedgerTransaction(s"ledger-${game.id}", game.id, "income", BigDecimal(100), 1, "测试收入"))
          _ <- DailyStatementTable.append(connection, DailyStatementRecord(s"statement-${game.id}", game.id, 1, DailyStatement(BigDecimal(10000), BigDecimal(100), BigDecimal(0), BigDecimal(10100), false), Instant.now().toString))
          loaded <- GameStateOperations.loadComplete(connection, game.id)
          updatedGame = game.copy(currentDay = 2)
          _ <- GameStateTable.update(connection, GameStateOperations.toSessionRecord(updatedGame))
          readGame <- GameStateTable.findById(connection, game.id)
          _ = assertEquals(readGame.map(_.currentDay), Some(2))
          _ = assertEquals(loaded.state.clinic.employees.map(_.id), List(staff.id))
          _ = assertEquals(loaded.state.clinic.facilities.map(_.id), List(facility.id))
          _ = assertEquals(loaded.state.inventory.items.map(_.id), List(inventoryItem.id))
          _ = assertEquals(loaded.patients.map(_.id), List(patient.id))
          _ = assertEquals(loaded.consultations.head.dialogueHistory.size, 1)
          _ = assertEquals(loaded.events.map(_.id), List(event.id))
          _ = assertEquals(loaded.reputationHistory.size, 1)
          _ = assertEquals(loaded.ledgerTransactions.size, 1)
          _ = assertEquals(loaded.dailyStatements.size, 1)
          changedStaff = staff.copy(satisfaction = 90)
          changedFacility = facility.copy(level = 2)
          changedEvent = event.copy(resolved = true)
          changedPatient = patient.copy(name = "修改后的患者")
          changedMedicine = medicine.copy(salePrice = BigDecimal(25))
          changedInventory = inventoryItem.copy(quantity = 5)
          changedConsultation = consultation.copy(status = ConsultationStatus.Completed, dialogueCount = 2)
          _ <- StaffTable.update(connection, changedStaff)
          _ <- FacilityTable.update(connection, changedFacility)
          _ <- EventTable.update(connection, changedEvent)
          _ <- PatientTable.update(connection, changedPatient)
          _ <- MedicineTable.update(connection, changedMedicine)
          _ <- InventoryTable.update(connection, changedInventory)
          _ <- ConsultationTable.update(connection, changedConsultation)
          loadedAfterUpdate <- GameStateOperations.loadComplete(connection, game.id)
          readStaff <- StaffTable.findById(connection, staff.id)
          readEvent <- EventTable.findById(connection, event.id)
          readPatient <- PatientTable.findById(connection, patient.id)
          readMedicine <- MedicineTable.findById(connection, medicine.id)
          readInventory <- InventoryTable.findById(connection, inventoryItem.id)
          readConsultation <- ConsultationTable.findById(connection, consultation.id)
          _ = assertEquals(readStaff.map(_.satisfaction), Some(90.0))
          _ = assertEquals(readEvent.map(_.resolved), Some(true))
          _ = assertEquals(readPatient.map(_.name), Some("修改后的患者"))
          _ = assertEquals(readMedicine.map(_.salePrice), Some(BigDecimal(25)))
          _ = assertEquals(readInventory.map(_.quantity), Some(5))
          _ = assertEquals(readConsultation.map(_.status), Some(ConsultationStatus.Completed))
          _ = assertEquals(loadedAfterUpdate.state.clinic.employees.map(_.satisfaction), List(90.0))
          _ = assertEquals(loadedAfterUpdate.state.clinic.facilities.map(_.level), List(2))
          _ = assertEquals(loadedAfterUpdate.state.inventory.items.map(_.quantity), List(5))
          _ = assertEquals(loadedAfterUpdate.patients.map(_.name), List("修改后的患者"))
          _ = assertEquals(loadedAfterUpdate.consultations.head.status, ConsultationStatus.Completed)
          _ <- ConsultationTable.delete(connection, consultation.id)
          _ <- EventTable.delete(connection, event.id)
          _ <- InventoryTable.delete(connection, inventoryItem.id)
          _ <- MedicineTable.delete(connection, medicine.id)
          _ <- PatientTable.delete(connection, patient.id)
          _ <- StaffTable.delete(connection, staff.id)
          _ <- FacilityTable.delete(connection, facility.id)
          deletedStaff <- StaffTable.findById(connection, staff.id)
          deletedPatient <- PatientTable.findById(connection, patient.id)
          deletedConsultation <- ConsultationTable.findById(connection, consultation.id)
          _ = assertEquals(deletedStaff, None)
          _ = assertEquals(deletedPatient, None)
          _ = assertEquals(deletedConsultation, None)
          _ <- GameStateOperations.delete(connection, game.id)
        yield ()
      }
    }
  }

  test("外键拒绝孤儿数据，删除游戏级联清理关联数据") {
    withDatabase { databaseSession =>
      for
        orphanResult <- databaseSession.withConnection { connection =>
            StaffTable.create(
            connection,
            "missing-game",
            Staff("orphan-staff", "孤儿员工", StaffRole.Nurse, BigDecimal(150), 10, 10, 50, true)
          ).attempt
        }
        _ = assert(orphanResult.isLeft)
        deletedGameId <- databaseSession.withTransactionConnection { connection =>
          for
            game <- newGame(connection)
            staff = Staff(s"cascade-staff-${game.id}", "级联员工", StaffRole.Nurse, BigDecimal(150), 10, 10, 50, true)
            facility = Facility(s"cascade-facility-${game.id}", "级联设施", 1, BigDecimal(0), 0)
            medicine = Medicine(s"cascade-medicine-${game.id}", "级联药品", BigDecimal(10), BigDecimal(20))
            inventory = InventoryItem(s"cascade-inventory-${game.id}", medicine, 1, "2030-01-01")
            patient = Patient(s"cascade-patient-${game.id}", "级联患者", PatientAttributes(1, 1, 1, 0.1))
            consultation = Consultation(
              s"cascade-consultation-${game.id}",
              patient,
              PatientEmotion.Calm,
              ConsultationStatus.Completed,
              Nil,
              0,
              None,
              BigDecimal(0),
              0
            )
            event = GameEvent(s"cascade-event-${game.id}", EventType.MedicalDispute, "级联事件", "级联测试", EventRiskLevel.Low, false)
            _ <- StaffTable.create(connection, game.id, staff)
            _ <- FacilityTable.create(connection, game.id, facility)
            _ <- MedicineTable.create(connection, medicine)
            _ <- InventoryTable.create(connection, game.id, inventory)
            _ <- PatientTable.create(connection, game.id, patient)
            _ <- ConsultationTable.create(connection, game.id, consultation)
            _ <- DialogueMessageTable.append(connection, consultation.id, DialogueMessage("patient", "测试", Instant.now().toString))
            _ <- EventTable.create(connection, game.id, event)
            _ <- ReputationTable.appendChange(connection, game.id, ReputationChange(1, "级联测试", Instant.now().toString))
            _ <- LedgerTable.append(connection, LedgerTransaction(s"cascade-ledger-${game.id}", game.id, "income", BigDecimal(1), 1, "级联测试"))
            _ <- DailyStatementTable.append(connection, DailyStatementRecord(s"cascade-statement-${game.id}", game.id, 1, DailyStatement(BigDecimal(0), BigDecimal(1), BigDecimal(0), BigDecimal(1), false), Instant.now().toString))
            _ <- GameStateOperations.delete(connection, game.id)
          yield game.id
        }
        patientsAfterDelete <- databaseSession.withConnection(connection => PatientTable.findByGameId(connection, deletedGameId))
        staffAfterDelete <- databaseSession.withConnection(connection => StaffTable.findByGameId(connection, deletedGameId))
        facilitiesAfterDelete <- databaseSession.withConnection(connection => FacilityTable.findByGameId(connection, deletedGameId))
        inventoryAfterDelete <- databaseSession.withConnection(connection => InventoryTable.findByGameId(connection, deletedGameId))
        consultationsAfterDelete <- databaseSession.withConnection(connection => ConsultationTable.findByGameId(connection, deletedGameId))
        dialogueAfterDelete <- databaseSession.withConnection(connection => DialogueMessageTable.findByConsultationId(connection, s"cascade-consultation-${deletedGameId}"))
        eventsAfterDelete <- databaseSession.withConnection(connection => EventTable.findByGameId(connection, deletedGameId))
        reputationAfterDelete <- databaseSession.withConnection(connection => ReputationTable.findChangesByGameId(connection, deletedGameId))
        ledgerAfterDelete <- databaseSession.withConnection(connection => LedgerTable.findByGameId(connection, deletedGameId))
        statementsAfterDelete <- databaseSession.withConnection(connection => DailyStatementTable.findByGameId(connection, deletedGameId))
        _ = assertEquals(patientsAfterDelete, Nil)
        _ = assertEquals(staffAfterDelete, Nil)
        _ = assertEquals(facilitiesAfterDelete, Nil)
        _ = assertEquals(inventoryAfterDelete, Nil)
        _ = assertEquals(consultationsAfterDelete, Nil)
        _ = assertEquals(dialogueAfterDelete, Nil)
        _ = assertEquals(eventsAfterDelete, Nil)
        _ = assertEquals(reputationAfterDelete, Nil)
        _ = assertEquals(ledgerAfterDelete, Nil)
        _ = assertEquals(statementsAfterDelete, Nil)
      yield ()
    }
  }

  test("事务失败时创建的游戏不会残留") {
    withDatabase { databaseSession =>
      val knownId = s"rollback-${UUID.randomUUID()}"
      val state = GameStateOperations.initialState("回滚测试", Instant.now())
        .copy(id = knownId)
      for
        failed <- databaseSession.withTransactionConnection { connection =>
          GameStateTable.insert(connection, GameStateOperations.toSessionRecord(state)) *>
            IO.raiseError[Unit](new RuntimeException("故意制造数据库事务错误"))
        }.attempt
        _ = assert(failed.isLeft)
        afterRollback <- databaseSession.withConnection(connection => GameStateTable.findById(connection, knownId))
        _ = assertEquals(afterRollback, None)
      yield ()
    }
  }
