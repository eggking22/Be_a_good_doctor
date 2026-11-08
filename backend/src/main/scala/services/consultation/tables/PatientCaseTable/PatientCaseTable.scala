// 病例表访问层：封装病例主记录及其关联症状的查询。
package services.consultation.tables

import cats.effect.IO
import cats.syntax.all.*
import services.consultation.objects.{CaseStatus, PatientCase}
import services.pharmacy.objects.{Disease, DiseaseAttributes}
import services.system.database.JdbcSupport

import java.sql.{Connection, ResultSet}
import java.time.OffsetDateTime

object PatientCaseTable:
  private val createSql =
    """
      |insert into patient_cases
      |  (id, game_id, patient_id, disease_id, disease_name, disease_difficulty, disease_extraction_probability, status, created_at)
      |values (?, ?, ?, ?, ?, ?, ?, ?, ?)
      |""".stripMargin

  def create(connection: Connection, gameId: String, patientCase: PatientCase): IO[Unit] =
    JdbcSupport.update(connection, createSql) { statement =>
      statement.setString(1, patientCase.id)
      statement.setString(2, gameId)
      statement.setString(3, patientCase.patientId)
      statement.setString(4, patientCase.disease.id)
      statement.setString(5, patientCase.disease.name)
      statement.setInt(6, patientCase.disease.attributes.difficulty)
      statement.setDouble(7, patientCase.disease.attributes.extractionProbability)
      statement.setString(8, patientCase.status.toString)
      statement.setObject(9, OffsetDateTime.now())
    }.map(_ => ())

  private val selectSql =
    """
      |select c.id, c.patient_id, c.status, c.disease_id, c.disease_name, c.disease_difficulty, c.disease_extraction_probability
      |from patient_cases c
      |""".stripMargin

  private val findByIdSql = selectSql + "where c.id = ?"

  def findById(connection: Connection, caseId: String): IO[Option[PatientCase]] =
    JdbcSupport.queryOne(connection, findByIdSql)(_.setString(1, caseId))(readCase)
      .flatMap {
        case Some(patientCase) => enrich(connection)(patientCase).map(Some(_))
        case None => IO.pure(Option.empty[PatientCase])
      }

  private val findActiveByPatientSql = selectSql + "where c.game_id = ? and c.patient_id = ? and c.status = 'Active' order by c.created_at desc"

  def findActiveByPatientId(connection: Connection, gameId: String, patientId: String): IO[Option[PatientCase]] =
    JdbcSupport.queryOne(connection, findActiveByPatientSql) { statement =>
      statement.setString(1, gameId)
      statement.setString(2, patientId)
    }(readCase).flatMap {
      case Some(patientCase) => enrich(connection)(patientCase).map(Some(_))
      case None => IO.pure(Option.empty[PatientCase])
    }

  private val findByGameIdSql = selectSql + "where c.game_id = ? order by c.created_at, c.id"

  def findByGameId(connection: Connection, gameId: String): IO[List[PatientCase]] =
    JdbcSupport.query(connection, findByGameIdSql)(_.setString(1, gameId))(readCase).flatMap(_.traverse(enrich(connection)))

  private val updateStatusSql = "update patient_cases set status = ? where id = ?"

  def updateStatus(connection: Connection, caseId: String, status: CaseStatus): IO[Unit] =
    JdbcSupport.updateExactlyOne(connection, updateStatusSql, s"未找到病例：$caseId") { statement =>
      statement.setString(1, status.toString)
      statement.setString(2, caseId)
    }

  private val deleteSql = "delete from patient_cases where id = ?"

  def delete(connection: Connection, caseId: String): IO[Unit] =
    JdbcSupport.deleteExactlyOne(connection, deleteSql, s"未找到病例：$caseId")(_.setString(1, caseId))

  private def enrich(connection: Connection)(patientCase: PatientCase): IO[PatientCase] =
    CaseSymptomTable.findByCaseId(connection, patientCase.id).map(symptoms => patientCase.copy(symptoms = symptoms))

  private def readCase(resultSet: ResultSet): PatientCase =
    PatientCase(
      id = resultSet.getString("id"),
      patientId = resultSet.getString("patient_id"),
      disease = Disease(
        id = resultSet.getString("disease_id"),
        name = resultSet.getString("disease_name"),
        attributes = DiseaseAttributes(
          difficulty = resultSet.getInt("disease_difficulty"),
          extractionProbability = resultSet.getDouble("disease_extraction_probability")
        )
      ),
      symptoms = Nil,
      status = CaseStatus.fromString(resultSet.getString("status")).fold(message => throw new IllegalStateException(message), identity)
    )
