// 单患者生成 API：读取当前游戏声誉，生成并持久化一名患者。
package services.patient.api

import cats.effect.IO
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import services.game.tables.GameStateTable
import services.patient.calculators.PatientGenerator
import services.patient.objects.Patient
import services.patient.tables.PatientTable
import services.system.HttpError
import services.system.api.APIMessage

import java.sql.Connection

final case class GeneratePatientAPIMessage(gameId: String) extends APIMessage[Patient]:
  override def plan(connection: Connection): IO[Patient] =
    val normalizedGameId = gameId.trim
    if normalizedGameId.isEmpty then IO.raiseError(HttpError.BadRequest("游戏 ID 不能为空。"))
    else
      GameStateTable.findById(connection, normalizedGameId).flatMap {
        case Some(game) =>
          val patient = PatientGenerator.generate(game.reputationScore)
          PatientTable.create(connection, normalizedGameId, patient).map(_ => patient)
        case None => IO.raiseError(HttpError.NotFound(s"未找到游戏：$normalizedGameId"))
      }

object GeneratePatientAPIMessage:
  // 请求体 JSON：{"gameId":"game-..."}。
  given Codec.AsObject[GeneratePatientAPIMessage] = deriveCodec
