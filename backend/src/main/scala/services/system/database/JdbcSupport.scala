// JDBC 通用工具：为领域 tables 层提供统一的阻塞数据库访问封装。
package services.system.database

import cats.effect.IO

import java.sql.{Connection, PreparedStatement, ResultSet}

object JdbcSupport:
  private[services] def execute(connection: Connection, sql: String): IO[Unit] =
    IO.blocking {
      val statement = connection.createStatement()
      try
        statement.execute(sql)
        ()
      finally statement.close()
    }

  private[services] def update(connection: Connection, sql: String)(bind: PreparedStatement => Unit): IO[Int] =
    IO.blocking {
      val statement = connection.prepareStatement(sql)
      try
        bind(statement)
        statement.executeUpdate()
      finally statement.close()
    }

  // Table 写入统一通过受影响行数判断目标是否存在，避免每个 Table 重复实现相同逻辑。
  private[services] def updateExactlyOne(
      connection: Connection,
      sql: String,
      notFoundMessage: String
  )(bind: PreparedStatement => Unit): IO[Unit] =
    update(connection, sql)(bind).flatMap { affectedRows =>
      if affectedRows == 1 then IO.unit
      else IO.raiseError(new NoSuchElementException(notFoundMessage))
    }

  private[services] def deleteExactlyOne(
      connection: Connection,
      sql: String,
      notFoundMessage: String
  )(bind: PreparedStatement => Unit): IO[Unit] =
    updateExactlyOne(connection, sql, notFoundMessage)(bind)

  private[services] def query[A](connection: Connection, sql: String)(bind: PreparedStatement => Unit)(read: ResultSet => A): IO[List[A]] =
    IO.blocking {
      val statement = connection.prepareStatement(sql)
      try
        bind(statement)
        val resultSet = statement.executeQuery()
        try
          val values = List.newBuilder[A]
          while resultSet.next() do values += read(resultSet)
          values.result()
        finally resultSet.close()
      finally statement.close()
    }

  private[services] def queryOne[A](connection: Connection, sql: String)(bind: PreparedStatement => Unit)(read: ResultSet => A): IO[Option[A]] =
    query(connection, sql)(bind)(read).map(_.headOption)
