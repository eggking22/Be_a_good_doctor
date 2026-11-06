// 事件表访问层：保存事件生成结果和处理状态。
package services.event.tables

import cats.effect.IO
import services.event.objects.{EventRiskLevel, EventType, GameEvent}
import services.system.database.JdbcSupport

import java.sql.Connection

object EventTable:
  private val createSql =
    "insert into game_events (id, game_id, event_type, title, description, risk_level, resolved) values (?, ?, ?, ?, ?, ?, ?)"

  def create(connection: Connection, gameId: String, event: GameEvent): IO[Unit] =
    JdbcSupport.update(connection, createSql) { statement =>
      statement.setString(1, event.id)
      statement.setString(2, gameId)
      statement.setString(3, event.eventType.toString)
      statement.setString(4, event.title)
      statement.setString(5, event.description)
      statement.setString(6, event.riskLevel.toString)
      statement.setBoolean(7, event.resolved)
    }.map(_ => ())

  private val findByIdSql = "select id, event_type, title, description, risk_level, resolved from game_events where id = ?"

  def findById(connection: Connection, eventId: String): IO[Option[GameEvent]] =
    JdbcSupport.queryOne(
      connection,
      findByIdSql
    )(_.setString(1, eventId))(readEvent)

  private val findByGameIdSql = "select id, event_type, title, description, risk_level, resolved from game_events where game_id = ? order by id"

  def findByGameId(connection: Connection, gameId: String): IO[List[GameEvent]] =
    JdbcSupport.query(
      connection,
      findByGameIdSql
    )(_.setString(1, gameId))(readEvent)

  private val updateSql = "update game_events set event_type = ?, title = ?, description = ?, risk_level = ?, resolved = ? where id = ?"

  def update(connection: Connection, event: GameEvent): IO[Unit] =
    JdbcSupport.updateExactlyOne(
      connection,
      updateSql,
      s"未找到事件：${event.id}"
    ) { statement =>
      statement.setString(1, event.eventType.toString)
      statement.setString(2, event.title)
      statement.setString(3, event.description)
      statement.setString(4, event.riskLevel.toString)
      statement.setBoolean(5, event.resolved)
      statement.setString(6, event.id)
    }

  private val deleteSql = "delete from game_events where id = ?"

  def delete(connection: Connection, eventId: String): IO[Unit] =
    JdbcSupport.deleteExactlyOne(connection, deleteSql, s"未找到事件：$eventId")(_.setString(1, eventId))

  private def readEvent(resultSet: java.sql.ResultSet): GameEvent =
    GameEvent(
      id = resultSet.getString("id"),
      eventType = EventType.fromString(resultSet.getString("event_type")).fold(message => throw new IllegalStateException(message), identity),
      title = resultSet.getString("title"),
      description = resultSet.getString("description"),
      riskLevel = EventRiskLevel.fromString(resultSet.getString("risk_level")).fold(message => throw new IllegalStateException(message), identity),
      resolved = resultSet.getBoolean("resolved")
    )
