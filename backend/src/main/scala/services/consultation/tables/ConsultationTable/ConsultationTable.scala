// 接诊表访问层：保存接诊过程和游戏外键；领域 Consultation 不需要暴露 gameId。
package services.consultation.tables

import cats.effect.IO
import cats.syntax.all.*
import services.consultation.objects.{Consultation, ConsultationStatus, TreatmentEvaluation}
import services.patient.objects.{Patient, PatientAttributes, PatientEmotion}
import services.system.database.JdbcSupport

import java.sql.{Connection, Types}

object ConsultationTable:
  // 接诊是过程记录：创建时通过 patient.id 关联患者，但领域对象不保存 gameId。
  private val createSql =
    """
      |insert into consultations
      |  (id, game_id, patient_id, emotion, status, dialogue_count, cash_change,
      |   reputation_change, treatment_matched, treatment_reputation_delta, treatment_reason)
      |values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      |""".stripMargin

  def create(connection: Connection, gameId: String, consultation: Consultation): IO[Unit] =
    JdbcSupport.update(connection, createSql) { statement =>
      statement.setString(1, consultation.id)
      statement.setString(2, gameId)
      statement.setString(3, consultation.patient.id)
      statement.setString(4, consultation.emotion.toString)
      statement.setString(5, consultation.status.toString)
      statement.setInt(6, consultation.dialogueCount)
      statement.setBigDecimal(7, consultation.cashChange.bigDecimal)
      statement.setDouble(8, consultation.reputationChange)
      bindTreatment(statement, consultation.treatmentEvaluation, 9)
    }.map(_ => ())

  private val findByIdSql = "where c.id = ?"

  def findById(connection: Connection, consultationId: String): IO[Option[Consultation]] =
    baseQuery(connection, findByIdSql)(_.setString(1, consultationId)).flatMap(_.headOption.traverse(enrichWithDialogue(connection)))

  private val findByGameIdSql = "where c.game_id = ? order by c.id"

  def findByGameId(connection: Connection, gameId: String): IO[List[Consultation]] =
    baseQuery(connection, findByGameIdSql)(_.setString(1, gameId)).flatMap(_.traverse(enrichWithDialogue(connection)))

  private val updateSql =
    """
      |update consultations set patient_id = ?, emotion = ?, status = ?, dialogue_count = ?,
      |cash_change = ?, reputation_change = ?, treatment_matched = ?,
      |treatment_reputation_delta = ?, treatment_reason = ? where id = ?
      |""".stripMargin

  def update(connection: Connection, consultation: Consultation): IO[Unit] =
    JdbcSupport.updateExactlyOne(
      connection,
      updateSql,
      s"未找到接诊记录：${consultation.id}"
    ) { statement =>
      statement.setString(1, consultation.patient.id)
      statement.setString(2, consultation.emotion.toString)
      statement.setString(3, consultation.status.toString)
      statement.setInt(4, consultation.dialogueCount)
      statement.setBigDecimal(5, consultation.cashChange.bigDecimal)
      statement.setDouble(6, consultation.reputationChange)
      bindTreatment(statement, consultation.treatmentEvaluation, 7)
      statement.setString(10, consultation.id)
    }

  private val deleteSql = "delete from consultations where id = ?"

  def delete(connection: Connection, consultationId: String): IO[Unit] =
    JdbcSupport.deleteExactlyOne(connection, deleteSql, s"未找到接诊记录：$consultationId")(_.setString(1, consultationId))

  private val selectSql =
    """
      |select c.id, c.emotion, c.status, c.dialogue_count, c.cash_change, c.reputation_change,
      |       c.treatment_matched, c.treatment_reputation_delta, c.treatment_reason,
      |       p.id as patient_id, p.name as patient_name, p.illness_difficulty,
      |       p.payment_willingness, p.communication_difficulty, p.dispute_risk
      |from consultations c
      |join patients p on p.id = c.patient_id
      |""".stripMargin

  private def baseQuery(
      connection: Connection,
      suffix: String
  )(bind: java.sql.PreparedStatement => Unit): IO[List[Consultation]] =
    JdbcSupport.query(
      connection,
      selectSql + suffix
    )(bind)(readConsultation)

  private def enrichWithDialogue(connection: Connection)(consultation: Consultation): IO[Consultation] =
    DialogueMessageTable.findByConsultationId(connection, consultation.id).map(messages =>
      consultation.copy(dialogueHistory = messages)
    )

  private def readConsultation(resultSet: java.sql.ResultSet): Consultation =
    Consultation(
      id = resultSet.getString("id"),
      patient = Patient(
        id = resultSet.getString("patient_id"),
        name = resultSet.getString("patient_name"),
        attributes = PatientAttributes(
          illnessDifficulty = resultSet.getInt("illness_difficulty"),
          paymentWillingness = resultSet.getInt("payment_willingness"),
          communicationDifficulty = resultSet.getInt("communication_difficulty"),
          disputeRisk = resultSet.getDouble("dispute_risk")
        )
      ),
      emotion = PatientEmotion.fromString(resultSet.getString("emotion")).fold(message => throw new IllegalStateException(message), identity),
      status = ConsultationStatus.fromString(resultSet.getString("status")).fold(message => throw new IllegalStateException(message), identity),
      dialogueHistory = Nil,
      dialogueCount = resultSet.getInt("dialogue_count"),
      treatmentEvaluation = readTreatment(resultSet),
      cashChange = BigDecimal(resultSet.getBigDecimal("cash_change")),
      reputationChange = resultSet.getDouble("reputation_change")
    )

  private def readTreatment(resultSet: java.sql.ResultSet): Option[TreatmentEvaluation] =
    Option(resultSet.getObject("treatment_matched")).map(_ =>
      TreatmentEvaluation(
        matched = resultSet.getBoolean("treatment_matched"),
        reputationDelta = resultSet.getDouble("treatment_reputation_delta"),
        reason = Option(resultSet.getString("treatment_reason")).getOrElse("")
      )
    )

  private def bindTreatment(
      statement: java.sql.PreparedStatement,
      treatment: Option[TreatmentEvaluation],
      startIndex: Int
  ): Unit = treatment match
    case Some(value) =>
      statement.setBoolean(startIndex, value.matched)
      statement.setDouble(startIndex + 1, value.reputationDelta)
      statement.setString(startIndex + 2, value.reason)
    case None =>
      statement.setNull(startIndex, Types.BOOLEAN)
      statement.setNull(startIndex + 1, Types.DOUBLE)
      statement.setNull(startIndex + 2, Types.VARCHAR)
