// 患者表访问层：只负责 patients 表的 CRUD，患者生成规则位于 calculators。
package services.patient.tables

import cats.effect.IO
import services.patient.objects.{Patient, PatientAttributes, PatientEmotion}
import services.system.database.JdbcSupport

import java.sql.Connection

object PatientTable:
  private val createSql =
    """
      |insert into patients
      |  (id, game_id, name, initial_emotion, illness_difficulty, payment_willingness, communication_difficulty, dispute_risk)
      |values (?, ?, ?, ?, ?, ?, ?, ?)
      |""".stripMargin

  def create(connection: Connection, gameId: String, patient: Patient): IO[Unit] =
    JdbcSupport.update(connection, createSql) { statement =>
      statement.setString(1, patient.id)
      statement.setString(2, gameId)
      statement.setString(3, patient.name)
      statement.setString(4, patient.initialEmotion.toString)
      statement.setInt(5, patient.attributes.illnessDifficulty)
      statement.setInt(6, patient.attributes.paymentWillingness)
      statement.setInt(7, patient.attributes.communicationDifficulty)
      statement.setDouble(8, patient.attributes.disputeRisk)
    }.map(_ => ())

  private val findByIdSql =
    "select id, name, initial_emotion, illness_difficulty, payment_willingness, communication_difficulty, dispute_risk from patients where id = ?"

  def findById(connection: Connection, patientId: String): IO[Option[Patient]] =
    JdbcSupport.queryOne(
      connection,
      findByIdSql
    )(_.setString(1, patientId))(readPatient)

  private val findByGameIdSql =
    "select id, name, initial_emotion, illness_difficulty, payment_willingness, communication_difficulty, dispute_risk from patients where game_id = ? order by id"

  def findByGameId(connection: Connection, gameId: String): IO[List[Patient]] =
    JdbcSupport.query(
      connection,
      findByGameIdSql
    )(_.setString(1, gameId))(readPatient)

  private val findByGameIdAndIdSql =
    "select id, name, initial_emotion, illness_difficulty, payment_willingness, communication_difficulty, dispute_risk from patients where game_id = ? and id = ?"

  def findByGameIdAndId(connection: Connection, gameId: String, patientId: String): IO[Option[Patient]] =
    JdbcSupport.queryOne(connection, findByGameIdAndIdSql) { statement =>
      statement.setString(1, gameId)
      statement.setString(2, patientId)
    }(readPatient)

  private val updateSql =
    """
      |update patients set name = ?, initial_emotion = ?, illness_difficulty = ?, payment_willingness = ?,
      |communication_difficulty = ?, dispute_risk = ? where id = ?
      |""".stripMargin

  def update(connection: Connection, patient: Patient): IO[Unit] =
    JdbcSupport.updateExactlyOne(
      connection,
      updateSql,
      s"未找到患者：${patient.id}"
    ) { statement =>
      statement.setString(1, patient.name)
      statement.setString(2, patient.initialEmotion.toString)
      statement.setInt(3, patient.attributes.illnessDifficulty)
      statement.setInt(4, patient.attributes.paymentWillingness)
      statement.setInt(5, patient.attributes.communicationDifficulty)
      statement.setDouble(6, patient.attributes.disputeRisk)
      statement.setString(7, patient.id)
    }

  private val deleteSql = "delete from patients where id = ?"

  def delete(connection: Connection, patientId: String): IO[Unit] =
    JdbcSupport.deleteExactlyOne(connection, deleteSql, s"未找到患者：$patientId")(_.setString(1, patientId))

  private def readPatient(resultSet: java.sql.ResultSet): Patient =
    Patient(
      id = resultSet.getString("id"),
      name = resultSet.getString("name"),
      attributes = PatientAttributes(
        illnessDifficulty = resultSet.getInt("illness_difficulty"),
        paymentWillingness = resultSet.getInt("payment_willingness"),
        communicationDifficulty = resultSet.getInt("communication_difficulty"),
        disputeRisk = resultSet.getDouble("dispute_risk")
      ),
      initialEmotion = PatientEmotion.fromString(resultSet.getString("initial_emotion")).fold(message => throw new IllegalStateException(message), identity)
    )
