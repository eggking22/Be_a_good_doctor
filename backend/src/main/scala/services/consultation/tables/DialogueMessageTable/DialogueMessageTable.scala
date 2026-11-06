// 对话历史表访问层：追加型记录，不提供普通 update。
package services.consultation.tables

import cats.effect.IO
import services.consultation.objects.DialogueMessage
import services.system.database.JdbcSupport

import java.sql.Connection

object DialogueMessageTable:
  private val appendSql =
    "insert into dialogue_messages (consultation_id, speaker, content, created_at) values (?, ?, ?, ?)"

  def append(connection: Connection, consultationId: String, message: DialogueMessage): IO[Unit] =
    JdbcSupport.update(connection, appendSql) { statement =>
      statement.setString(1, consultationId)
      statement.setString(2, message.speaker)
      statement.setString(3, message.content)
      statement.setObject(4, java.time.OffsetDateTime.parse(message.createdAt))
    }.map(_ => ())

  private val findByConsultationIdSql =
    "select speaker, content, created_at from dialogue_messages where consultation_id = ? order by id"

  def findByConsultationId(connection: Connection, consultationId: String): IO[List[DialogueMessage]] =
    JdbcSupport.query(
      connection,
      findByConsultationIdSql
    )(_.setString(1, consultationId)) { resultSet =>
      DialogueMessage(
        speaker = resultSet.getString("speaker"),
        content = resultSet.getString("content"),
        createdAt = resultSet.getObject("created_at", classOf[java.time.OffsetDateTime]).toString
      )
    }
