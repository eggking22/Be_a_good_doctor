// API 消息路由器：按服务名和 API 名称分发请求，并统一处理事务和错误。
package services.system.api

import cats.effect.IO
import io.circe.Json
import org.http4s.{HttpRoutes, InvalidMessageBodyFailure, Response, Status}
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.io.*
import services.system.HttpError
import services.system.database.DatabaseSession

object APIMessageRouter:
  def routes(
      databaseSession: DatabaseSession,
      serviceMessages: List[ApiServiceMessages]
  ): HttpRoutes[IO] =
    val messagesByName =
      serviceMessages
        .flatMap(service => service.messages.map(message => (service.serviceName, message.apiName) -> message))
        .toMap

    HttpRoutes.of[IO] {
      case req @ POST -> Root / "api" / "messages" / serviceName / "requests" / apiName =>
        handleErrors {
          for
            message <- IO.fromOption(messagesByName.get(serviceName -> normalize(apiName)))(
              HttpError.NotFound(s"不支持的 API：$serviceName/$apiName")
            )
            payload <- req.as[Json]
            response <- message.accessMode match
              case APIAccessMode.ReadOnly => databaseSession.withReadOnlyConnection(connection => message.planJson(payload, connection))
              case APIAccessMode.Transactional => databaseSession.withTransactionConnection(connection => message.planJson(payload, connection))
            result <- Ok(response)
          yield result
        }
    }

  private def normalize(value: String): String = value.trim.toLowerCase

  private def handleErrors(action: IO[Response[IO]]): IO[Response[IO]] =
    action.handleErrorWith {
      case error: InvalidMessageBodyFailure => BadRequest(errorResponse(error.getMessage))
      case error: HttpError.BadRequest => BadRequest(errorResponse(error.getMessage))
      case error: HttpError.NotFound => NotFound(errorResponse(error.getMessage))
      case error: HttpError.Conflict => Conflict(errorResponse(error.getMessage))
      case error =>
        IO.pure(Response[IO](Status.InternalServerError).withEntity(errorResponse(error.getMessage)))
    }

  private def errorResponse(message: String): Json =
    Json.obj("error" -> Json.fromString(Option(message).getOrElse("未知错误")))
