// 健康检查路由：只负责确认 HTTP 服务已经启动。
package app

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.io.*

object HealthRouter:
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "api" / "health" =>
      Ok(io.circe.Json.obj("status" -> io.circe.Json.fromString("ok")))
  }
