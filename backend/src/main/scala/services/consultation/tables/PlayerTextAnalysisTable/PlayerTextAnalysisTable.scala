// 玩家文字分析表访问层：只追加经后端白名单校验后的模型理解结果。
package services.consultation.tables

import cats.effect.IO
import services.consultation.objects.{PlayerAttitude, PlayerIntent, PlayerTextAnalysis}
import services.system.database.JdbcSupport

import java.sql.Connection
import java.time.OffsetDateTime

object PlayerTextAnalysisTable:
  private val appendSql =
    """
      |insert into player_text_analyses
      |  (consultation_id, attitude, intent, mentioned_medicine_ids, confidence, created_at)
      |values (?, ?, ?, ?, ?, ?)
      |""".stripMargin

  def append(connection: Connection, consultationId: String, analysis: PlayerTextAnalysis): IO[Unit] =
    JdbcSupport.update(connection, appendSql) { statement =>
      statement.setString(1, consultationId)
      statement.setString(2, analysis.attitude.toString)
      statement.setString(3, analysis.intent.toString)
      statement.setString(4, analysis.mentionedMedicineIds.mkString("\n"))
      statement.setDouble(5, analysis.confidence)
      statement.setObject(6, OffsetDateTime.now())
    }.map(_ => ())

  private val findByConsultationIdSql =
    """
      |select attitude, intent, mentioned_medicine_ids, confidence
      |from player_text_analyses where consultation_id = ? order by id
      |""".stripMargin

  def findByConsultationId(connection: Connection, consultationId: String): IO[List[PlayerTextAnalysis]] =
    JdbcSupport.query(connection, findByConsultationIdSql)(_.setString(1, consultationId)) { resultSet =>
      PlayerTextAnalysis(
        attitude = PlayerAttitude.fromString(resultSet.getString("attitude")).fold(message => throw new IllegalStateException(message), identity),
        intent = PlayerIntent.fromString(resultSet.getString("intent")).fold(message => throw new IllegalStateException(message), identity),
        mentionedMedicineIds = Option(resultSet.getString("mentioned_medicine_ids")).filter(_.nonEmpty).map(_.split("\n").toList).getOrElse(Nil),
        confidence = resultSet.getDouble("confidence")
      )
    }
