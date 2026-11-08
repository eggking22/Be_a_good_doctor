// 病例症状表访问层：只负责病例症状的持久化和读取。
package services.consultation.tables

import cats.effect.IO
import services.consultation.objects.CaseSymptom
import services.pharmacy.objects.Symptom
import services.system.database.JdbcSupport

import java.sql.Connection

object CaseSymptomTable:
  private val createSql =
    "insert into case_symptoms (case_id, symptom_id, symptom_name, severity, revealed) values (?, ?, ?, ?, ?)"

  def create(connection: Connection, caseId: String, caseSymptom: CaseSymptom): IO[Unit] =
    JdbcSupport.update(connection, createSql) { statement =>
      statement.setString(1, caseId)
      statement.setString(2, caseSymptom.symptom.id)
      statement.setString(3, caseSymptom.symptom.name)
      statement.setInt(4, caseSymptom.severity)
      statement.setBoolean(5, caseSymptom.revealed)
    }.map(_ => ())

  def createAll(connection: Connection, caseId: String, symptoms: List[CaseSymptom]): IO[Unit] =
    symptoms.foldLeft(IO.unit)((program, symptom) => program *> create(connection, caseId, symptom))

  private val findByCaseIdSql =
    """
      |select cs.symptom_id, cs.symptom_name, cs.severity, cs.revealed
      |from case_symptoms cs where cs.case_id = ? order by cs.symptom_id
      |""".stripMargin

  def findByCaseId(connection: Connection, caseId: String): IO[List[CaseSymptom]] =
    JdbcSupport.query(connection, findByCaseIdSql)(_.setString(1, caseId)) { resultSet =>
      CaseSymptom(
        symptom = Symptom(resultSet.getString("symptom_id"), resultSet.getString("symptom_name")),
        severity = resultSet.getInt("severity"),
        revealed = resultSet.getBoolean("revealed")
      )
    }

  private val updateRevealedSql =
    "update case_symptoms set revealed = ? where case_id = ? and symptom_id = ?"

  def updateRevealed(connection: Connection, caseId: String, symptomId: String, revealed: Boolean): IO[Unit] =
    JdbcSupport.updateExactlyOne(connection, updateRevealedSql, s"未找到病例症状：$caseId / $symptomId") { statement =>
      statement.setBoolean(1, revealed)
      statement.setString(2, caseId)
      statement.setString(3, symptomId)
    }
