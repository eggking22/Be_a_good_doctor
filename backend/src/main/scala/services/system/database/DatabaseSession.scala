// 数据库会话：管理连接池、连接生命周期以及 API 事务边界。
package services.system.database

import cats.effect.{IO, Resource}
import cats.syntax.all.*
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}

import java.sql.Connection

final class DatabaseSession private (dataSource: HikariDataSource):
  private def connectionResource: Resource[IO, Connection] =
    Resource.make(IO.blocking(dataSource.getConnection)) { connection =>
      IO.blocking(connection.close()).handleErrorWith(_ => IO.unit)
    }

  def withConnection[A](operation: Connection => IO[A]): IO[A] =
    connectionResource.use(operation)

  def withReadOnlyConnection[A](operation: Connection => IO[A]): IO[A] =
    connectionResource.use { connection =>
      for
        _ <- IO.blocking(connection.setReadOnly(true))
        value <- operation(connection)
      yield value
    }

  def withTransactionConnection[A](operation: Connection => IO[A]): IO[A] =
    connectionResource.use { connection =>
      for
        _ <- IO.blocking(connection.setAutoCommit(false))
        result <- operation(connection).attempt
        value <- result match
          case Right(value) =>
            IO.blocking(connection.commit()).attempt.flatMap {
              case Right(_) => IO.pure(value)
              case Left(commitError) =>
                IO.blocking(connection.rollback()).attempt *> IO.raiseError[A](commitError)
            }
          case Left(operationError) =>
            IO.blocking(connection.rollback()).attempt *> IO.raiseError[A](operationError)
      yield value
    }

object DatabaseSession:
  def initialize: Resource[IO, DatabaseSession] =
    for
      config <- Resource.eval(IO.pure(DatabaseConfig.load))
      dataSource <- Resource.make(createDataSource(config))(source => IO.blocking(source.close()))
    yield new DatabaseSession(dataSource)

  private def createDataSource(config: DatabaseConfig): IO[HikariDataSource] =
    IO.blocking {
      Class.forName("org.postgresql.Driver")
      val hikariConfig = new HikariConfig()
      hikariConfig.setJdbcUrl(config.jdbcUrl)
      hikariConfig.setUsername(config.user)
      hikariConfig.setPassword(config.password)
      hikariConfig.setMaximumPoolSize(config.maxPoolSize)
      hikariConfig.setConnectionTimeout(config.connectionTimeoutMs)
      hikariConfig.setPoolName("ai-clinic-simulation-pool")
      new HikariDataSource(hikariConfig)
    }
