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
      |  (id, player_name, current_day, current_minute, cash, reputation, bankrupt, created_at, updated_at)
      |values (?, ?, ?, ?, ?, ?, ?, ?, ?)
      |""".stripMargin

  private val updateSql =
    """
      |update game_sessions
      |set current_day = ?, current_minute = ?, cash = ?, reputation = ?, bankrupt = ?, updated_at = ?
      |where id = ?
      |""".stripMargin

  private val deleteSql = "delete from game_sessions where id = ?"
  private val findByIdSql =
    "select id, player_name, current_day, current_minute, cash, reputation, bankrupt, created_at, updated_at from game_sessions where id = ?"
  private val findAllSql =
    "select id, player_name, current_day, current_minute, cash, reputation, bankrupt, created_at, updated_at from game_sessions order by updated_at desc, id desc"

  def insert(connection: Connection, record: GameSessionRecord): IO[Unit] =
    JdbcSupport.update(connection, insertSql) { statement =>
      statement.setString(1, record.id)
      statement.setString(2, record.playerName)
      statement.setInt(3, record.currentDay)
      statement.setInt(4, record.currentMinute)
      statement.setBigDecimal(5, record.cash.bigDecimal)
      statement.setDouble(6, record.reputationScore)
      statement.setBoolean(7, record.bankrupt)
      statement.setObject(8, record.createdAt)
      statement.setObject(9, record.updatedAt)
    }.map(_ => ())

  def update(connection: Connection, record: GameSessionRecord): IO[Unit] =
    JdbcSupport.updateExactlyOne(connection, updateSql, s"未找到游戏：${record.id}") { statement =>
      statement.setInt(1, record.currentDay)
      statement.setInt(2, record.currentMinute)
      statement.setBigDecimal(3, record.cash.bigDecimal)
      statement.setDouble(4, record.reputationScore)
      statement.setBoolean(5, record.bankrupt)
      statement.setObject(6, record.updatedAt)
      statement.setString(7, record.id)
    }

  // 删除游戏会话；子表通过 FOREIGN KEY ... ON DELETE CASCADE 自动清理。
  def delete(connection: Connection, gameId: String): IO[Unit] =
    JdbcSupport.deleteExactlyOne(connection, deleteSql, s"未找到游戏：$gameId") { statement =>
      statement.setString(1, gameId)
    }

  // 新游戏替换当前存档时允许旧存档已不存在，避免客户端过期指针阻塞开局。
  def deleteIfExists(connection: Connection, gameId: String): IO[Unit] =
    JdbcSupport.update(connection, deleteSql)(_.setString(1, gameId)).map(_ => ())

  def findById(connection: Connection, gameId: String): IO[Option[GameSessionRecord]] =
    JdbcSupport.queryOne(connection, findByIdSql) { statement =>
      statement.setString(1, gameId)
    } { resultSet =>
      GameSessionRecord(
        id = resultSet.getString("id"),
        playerName = resultSet.getString("player_name"),
        currentDay = resultSet.getInt("current_day"),
        currentMinute = resultSet.getInt("current_minute"),
        cash = BigDecimal(resultSet.getBigDecimal("cash")),
        reputationScore = resultSet.getDouble("reputation"),
        bankrupt = resultSet.getBoolean("bankrupt"),
        createdAt = resultSet.getObject("created_at", classOf[java.time.OffsetDateTime]),
        updatedAt = resultSet.getObject("updated_at", classOf[java.time.OffsetDateTime])
      )
    }

  def findAll(connection: Connection): IO[List[GameSessionRecord]] =
    JdbcSupport.query(connection, findAllSql)(_ => ())(readRecord)

  private def readRecord(resultSet: java.sql.ResultSet): GameSessionRecord =
    GameSessionRecord(
      id = resultSet.getString("id"),
      playerName = resultSet.getString("player_name"),
      currentDay = resultSet.getInt("current_day"),
      currentMinute = resultSet.getInt("current_minute"),
      cash = BigDecimal(resultSet.getBigDecimal("cash")),
      reputationScore = resultSet.getDouble("reputation"),
      bankrupt = resultSet.getBoolean("bankrupt"),
      createdAt = resultSet.getObject("created_at", classOf[java.time.OffsetDateTime]),
      updatedAt = resultSet.getObject("updated_at", classOf[java.time.OffsetDateTime])
    )
