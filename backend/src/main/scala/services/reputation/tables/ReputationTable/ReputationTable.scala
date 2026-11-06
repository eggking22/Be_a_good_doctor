// 声誉表访问层：记录每次声誉变化的原因，便于日结算和历史追踪。
package services.reputation.tables

import cats.effect.IO
import services.reputation.objects.ReputationChange
import services.system.database.JdbcSupport

import java.sql.Connection

object ReputationTable:
  // 声誉变化是历史记录，采用追加和查询，不直接更新历史行。
  private val appendChangeSql =
    "insert into reputation_records (id, game_id, score, delta, reason, created_at) values (?, ?, ?, ?, ?, ?)"

  def appendChange(connection: Connection, gameId: String, change: ReputationChange): IO[Unit] =
    JdbcSupport.update(connection, appendChangeSql) { statement =>
      statement.setString(1, s"reputation-${java.util.UUID.randomUUID()}")
      statement.setString(2, gameId)
      statement.setNull(3, java.sql.Types.DOUBLE)
      statement.setDouble(4, change.amount)
      statement.setString(5, change.reason)
      statement.setObject(6, java.time.OffsetDateTime.parse(change.changedAt))
    }.map(_ => ())

  private val findChangesByGameIdSql =
    "select delta, reason, created_at from reputation_records where game_id = ? order by created_at, id"

  def findChangesByGameId(connection: Connection, gameId: String): IO[List[ReputationChange]] =
    JdbcSupport.query(
      connection,
      findChangesByGameIdSql
    )(_.setString(1, gameId)) { resultSet =>
      ReputationChange(
        amount = resultSet.getDouble("delta"),
        reason = resultSet.getString("reason"),
        changedAt = resultSet.getObject("created_at", classOf[java.time.OffsetDateTime]).toString
      )
    }
