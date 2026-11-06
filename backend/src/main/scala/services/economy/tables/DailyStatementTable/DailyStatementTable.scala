// 日结算历史表：只提供追加和按游戏查询，当前财务状态仍由 FinanceState 保存。
package services.economy.tables

import cats.effect.IO
import services.economy.objects.{DailyStatement, DailyStatementRecord}
import services.system.database.JdbcSupport

import java.sql.Connection

object DailyStatementTable:
  private val appendSql =
    """
      |insert into daily_statements
      |  (id, game_id, day, opening_cash, income, expense, closing_cash, bankrupt, created_at)
      |values (?, ?, ?, ?, ?, ?, ?, ?, ?)
      |""".stripMargin

  def append(connection: Connection, record: DailyStatementRecord): IO[Unit] =
    JdbcSupport.update(connection, appendSql) { statement =>
      statement.setString(1, record.id)
      statement.setString(2, record.gameId)
      statement.setInt(3, record.day)
      statement.setBigDecimal(4, record.statement.openingCash.bigDecimal)
      statement.setBigDecimal(5, record.statement.income.bigDecimal)
      statement.setBigDecimal(6, record.statement.expense.bigDecimal)
      statement.setBigDecimal(7, record.statement.closingCash.bigDecimal)
      statement.setBoolean(8, record.statement.bankrupt)
      statement.setObject(9, java.time.OffsetDateTime.parse(record.createdAt))
    }.map(_ => ())

  private val findByGameIdSql =
    """
      |select id, game_id, day, opening_cash, income, expense, closing_cash, bankrupt, created_at
      |from daily_statements where game_id = ? order by day
      |""".stripMargin

  def findByGameId(connection: Connection, gameId: String): IO[List[DailyStatementRecord]] =
    JdbcSupport.query(
      connection,
      findByGameIdSql
    )(_.setString(1, gameId)) { resultSet =>
      DailyStatementRecord(
        id = resultSet.getString("id"),
        gameId = resultSet.getString("game_id"),
        day = resultSet.getInt("day"),
        statement = DailyStatement(
          openingCash = BigDecimal(resultSet.getBigDecimal("opening_cash")),
          income = BigDecimal(resultSet.getBigDecimal("income")),
          expense = BigDecimal(resultSet.getBigDecimal("expense")),
          closingCash = BigDecimal(resultSet.getBigDecimal("closing_cash")),
          bankrupt = resultSet.getBoolean("bankrupt")
        ),
        createdAt = resultSet.getObject("created_at", classOf[java.time.OffsetDateTime]).toString
      )
    }
