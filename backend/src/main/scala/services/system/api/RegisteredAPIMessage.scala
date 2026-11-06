// API 注册描述：API 名称显式声明，避免依赖类名反射生成路径。
package services.system.api

import cats.effect.IO
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.*
import services.system.HttpError

import java.sql.Connection
import scala.reflect.ClassTag

final case class RegisteredAPIMessage(
    apiName: String,
    accessMode: APIAccessMode,
    planJson: (Json, Connection) => IO[Json]
)

object RegisteredAPIMessage:
  def api[Message <: APIMessage[Response], Response](using
      Decoder[Message],
      Encoder[Response],
      ClassTag[Message]
  ): RegisteredAPIMessage =
    build(APIAccessMode.Transactional)

  def query[Message <: APIMessage[Response], Response](using
      Decoder[Message],
      Encoder[Response],
      ClassTag[Message]
  ): RegisteredAPIMessage =
    build(APIAccessMode.ReadOnly)

  private def build[Message <: APIMessage[Response], Response](
      accessMode: APIAccessMode
  )(using Decoder[Message], Encoder[Response], ClassTag[Message]): RegisteredAPIMessage =
    RegisteredAPIMessage(
      apiName = deriveApiName[Message],
      accessMode = accessMode,
      planJson = (payload, connection) =>
        for
          message <- IO.fromEither(
            payload.as[Message].left.map(error => HttpError.BadRequest(s"请求体格式错误：${error.getMessage}"))
          )
          response <- message.plan(connection)
        yield response.asJson
    )

  private def deriveApiName[Message](using tag: ClassTag[Message]): String =
    tag.runtimeClass.getSimpleName.stripSuffix("Message").toLowerCase(java.util.Locale.ROOT)
