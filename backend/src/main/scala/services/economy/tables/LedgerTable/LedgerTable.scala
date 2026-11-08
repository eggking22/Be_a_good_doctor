// 现金流水表访问层：历史流水只允许追加和按游戏查询，表结构由迁移层管理。
package services.economy.tables

import cats.effect.IO
import services.economy.objects.LedgerTransaction
import services.system.database.JdbcSupport

import java.sql.Connection

object LedgerTable:
  // 现金流水是历史记录，采用追加和按游戏查询，不提供普通 update。
  private val appendSql =
    "insert into ledger_transactions (id, game_id, transaction_type, amount, occurred_on, description) values (?, ?, ?, ?, ?, ?)"

  def append(connection: Connection, transaction: LedgerTransaction): IO[Unit] =
    JdbcSupport.update(connection, appendSql) { statement =>
      statement.setString(1, transaction.id)
      statement.setString(2, transaction.gameId)
      statement.setString(3, transaction.transactionType)
      statement.setBigDecimal(4, transaction.amount.bigDecimal)
      statement.setInt(5, transaction.occurredOn)
      statement.setString(6, transaction.description)
    }.map(_ => ())

  private val findByGameIdSql =
    "select id, game_id, transaction_type, amount, occurred_on, description from ledger_transactions where game_id = ? order by occurred_on, id"

  def findByGameId(connection: Connection, gameId: String): IO[List[LedgerTransaction]] =
    JdbcSupport.query(
      connection,
      findByGameIdSql
    )(_.setString(1, gameId)) { resultSet =>
      LedgerTransaction(
        id = resultSet.getString("id"),
        gameId = resultSet.getString("game_id"),
        transactionType = resultSet.getString("transaction_type"),
        amount = BigDecimal(resultSet.getBigDecimal("amount")),
        occurredOn = resultSet.getInt("occurred_on"),
        description = resultSet.getString("description")
      )
    }

  private val findByGameIdAndDaySql =
    "select id, game_id, transaction_type, amount, occurred_on, description from ledger_transactions where game_id = ? and occurred_on = ? order by id"

  def findByGameIdAndDay(connection: Connection, gameId: String, day: Int): IO[List[LedgerTransaction]] =
    JdbcSupport.query(connection, findByGameIdAndDaySql) { statement =>
      statement.setString(1, gameId)
      statement.setInt(2, day)
    } { resultSet =>
      LedgerTransaction(
        id = resultSet.getString("id"),
        gameId = resultSet.getString("game_id"),
        transactionType = resultSet.getString("transaction_type"),
        amount = BigDecimal(resultSet.getBigDecimal("amount")),
        occurredOn = resultSet.getInt("occurred_on"),
        description = resultSet.getString("description")
      )
    }
