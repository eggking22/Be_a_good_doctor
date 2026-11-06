// 应用启动入口：初始化数据库、注册统一路由并启动 HTTP 服务器。
package app

import cats.effect.{IO, IOApp}
import com.comcast.ip4s.{Port, host, port}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import services.system.database.{DatabaseSchemaInitializer, DatabaseSession}

object Main extends IOApp.Simple:
  private val logger = Slf4jLogger.getLogger[IO]

  private val httpPort: Port =
    sys.env
      .get("HTTP_PORT")
      .flatMap(_.toIntOption)
      .flatMap(Port.fromInt)
      .getOrElse(port"8080")

  override def run: IO[Unit] =
    DatabaseSession.initialize.use { databaseSession =>
      for
        _ <- databaseSession.withTransactionConnection(DatabaseSchemaInitializer.initialize)
        httpApp = Logger.httpApp(logHeaders = true, logBody = false)(ApiRouter.httpApp(databaseSession))
        _ <- logger.info(s"Starting AI clinic simulation backend on http://0.0.0.0:${httpPort.value}")
        _ <- EmberServerBuilder
          .default[IO]
          .withHost(host"0.0.0.0")
          .withPort(httpPort)
          .withHttpApp(httpApp)
          .build
          .useForever
      yield ()
    }
