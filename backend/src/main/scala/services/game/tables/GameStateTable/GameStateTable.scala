// 游戏状态表访问层：只负责 game_sessions 表的 CRUD，不包含游戏规则。
package services.game.tables

import cats.effect.IO
import services.game.objects.GameSessionRecord
import services.system.database.JdbcSupport

import java.sql.Connection

object GameStateTable:
  private val insertSql =
    """
      |insert into game_sessions
      |  (id, player_name, current_day, cash, reputation, bankrupt, created_at, updated_at)
      |values (?, ?, ?, ?, ?, ?, ?, ?)
      |""".stripMargin

  private val updateSql =
    """
      |update game_sessions
      |set current_day = ?, cash = ?, reputation = ?, bankrupt = ?, updated_at = ?
      |where id = ?
      |""".stripMargin

  private val deleteSql = "delete from game_sessions where id = ?"
  private val findByIdSql =
    "select id, player_name, current_day, cash, reputation, bankrupt, created_at, updated_at from game_sessions where id = ?"

  def insert(connection: Connection, record: GameSessionRecord): IO[Unit] =
    JdbcSupport.update(connection, insertSql) { statement =>
      statement.setString(1, record.id)
      statement.setString(2, record.playerName)
      statement.setInt(3, record.currentDay)
      statement.setBigDecimal(4, record.cash.bigDecimal)
      statement.setDouble(5, record.reputationScore)
      statement.setBoolean(6, record.bankrupt)
      statement.setObject(7, record.createdAt)
      statement.setObject(8, record.updatedAt)
    }.map(_ => ())

  def update(connection: Connection, record: GameSessionRecord): IO[Unit] =
    JdbcSupport.updateExactlyOne(connection, updateSql, s"未找到游戏：${record.id}") { statement =>
      statement.setInt(1, record.currentDay)
      statement.setBigDecimal(2, record.cash.bigDecimal)
      statement.setDouble(3, record.reputationScore)
      statement.setBoolean(4, record.bankrupt)
      statement.setObject(5, record.updatedAt)
      statement.setString(6, record.id)
    }

  // 删除游戏会话；子表通过 FOREIGN KEY ... ON DELETE CASCADE 自动清理。
  def delete(connection: Connection, gameId: String): IO[Unit] =
    JdbcSupport.deleteExactlyOne(connection, deleteSql, s"未找到游戏：$gameId") { statement =>
      statement.setString(1, gameId)
    }

  def findById(connection: Connection, gameId: String): IO[Option[GameSessionRecord]] =
    JdbcSupport.queryOne(connection, findByIdSql) { statement =>
      statement.setString(1, gameId)
    } { resultSet =>
      GameSessionRecord(
        id = resultSet.getString("id"),
        playerName = resultSet.getString("player_name"),
        currentDay = resultSet.getInt("current_day"),
        cash = BigDecimal(resultSet.getBigDecimal("cash")),
        reputationScore = resultSet.getDouble("reputation"),
        bankrupt = resultSet.getBoolean("bankrupt"),
        createdAt = resultSet.getObject("created_at", classOf[java.time.OffsetDateTime]),
        updatedAt = resultSet.getObject("updated_at", classOf[java.time.OffsetDateTime])
      )
    }
