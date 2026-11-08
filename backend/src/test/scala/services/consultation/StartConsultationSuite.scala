// 开始接诊集成测试：验证病例快照、隐藏疾病和重复接诊保护。
package services.consultation

import cats.effect.IO
import io.circe.syntax.*
import munit.CatsEffectSuite
import services.consultation.api.StartConsultationAPIMessage
import services.consultation.objects.{CaseStatus, ConsultationStatus}
import services.consultation.tables.{CaseSymptomTable, ConsultationTable, PatientCaseTable}
import services.game.api.StartGameAPIMessage
import services.patient.tables.PatientTable
import services.pharmacy.tables.{DiseaseTable, SymptomTable}
import services.system.HttpError
import services.system.database.{DatabaseSchemaInitializer, DatabaseSession}

import java.sql.Connection

final class StartConsultationSuite extends CatsEffectSuite:
  private def withRollback(operation: Connection => IO[Unit]): IO[Unit] =
    val rollback = new RuntimeException("开始接诊测试回滚")
    DatabaseSession.initialize.use { databaseSession =>
      databaseSession.withTransactionConnection { connection =>
        DatabaseSchemaInitializer.initialize(connection) *>
          operation(connection) *>
          IO.raiseError[Unit](rollback)
      }.attempt.flatMap {
        case Left(error) if error eq rollback => IO.unit
        case Left(error) => IO.raiseError(error)
        case Right(_) => IO.raiseError(new IllegalStateException("开始接诊测试未回滚"))
      }
    }

  test("开始接诊会创建隐藏疾病、症状快照和接诊记录") {
    withRollback { connection =>
      for
        game <- StartGameAPIMessage("接诊流程测试").plan(connection)
        patient <- PatientTable.findByGameId(connection, game.id).map(_.headOption).flatMap {
          case Some(value) => IO.pure(value)
          case None => IO.raiseError(new NoSuchElementException("开局未生成患者"))
        }
        response <- StartConsultationAPIMessage(game.id, patient.id).plan(connection)
        consultation <- ConsultationTable.findById(connection, response.consultationId).flatMap {
          case Some(value) => IO.pure(value)
          case None => IO.raiseError(new NoSuchElementException("未创建接诊记录"))
        }
        caseId <- IO.fromOption(consultation.caseId)(new NoSuchElementException("接诊未关联病例"))
        patientCase <- PatientCaseTable.findById(connection, caseId).flatMap {
          case Some(value) => IO.pure(value)
          case None => IO.raiseError(new NoSuchElementException("未创建病例"))
        }
        _ = assertEquals(patient.attributes.illnessDifficulty, 3)
        _ = assertEquals(patientCase.disease.id, "disease-seasonal-flu")
        _ = assertEquals(patientCase.disease.attributes.difficulty, 3)
        _ = assertEquals(patientCase.status, CaseStatus.Active)
        _ = assertEquals(consultation.status, ConsultationStatus.InProgress)
        _ = assertEquals(response.status, ConsultationStatus.InProgress)
        _ = assert(!response.asJson.noSpaces.contains("disease"))
        _ = assertEquals(patientCase.symptoms.map(_.symptom.name).toSet, Set("发热", "咳嗽", "头痛"))
        _ = assert(patientCase.symptoms.forall(symptom => !symptom.revealed && symptom.severity == 3))
        firstSymptom = patientCase.symptoms.head
        _ <- DiseaseTable.update(connection, patientCase.disease.copy(name = "目录修改后的疾病", attributes = patientCase.disease.attributes.copy(difficulty = 5)))
        _ <- SymptomTable.update(connection, firstSymptom.symptom.copy(name = "目录修改后的症状"))
        reloadedCase <- PatientCaseTable.findById(connection, caseId).flatMap {
          case Some(value) => IO.pure(value)
          case None => IO.raiseError(new NoSuchElementException("无法重新读取病例"))
        }
        _ = assertEquals(reloadedCase.disease.name, "流感")
        _ = assertEquals(reloadedCase.disease.attributes.difficulty, 3)
        _ = assertEquals(reloadedCase.symptoms.find(_.symptom.id == firstSymptom.symptom.id).map(_.symptom.name), Some(firstSymptom.symptom.name))
        duplicate <- StartConsultationAPIMessage(game.id, patient.id).plan(connection).attempt
        _ = duplicate match
          case Left(_: HttpError.Conflict) => assert(true)
          case other => fail(s"重复接诊应被拒绝，实际结果：$other")
        storedSymptoms <- CaseSymptomTable.findByCaseId(connection, caseId)
        _ = assertEquals(storedSymptoms, reloadedCase.symptoms)
      yield ()
    }
  }
