// 接下一位患者前以 10% 概率检查一次康复回访。
package services.reputation.api

import cats.effect.IO
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import services.reputation.objects.RecoveryFollowUp
import services.reputation.utils.ReputationOperations
import services.system.api.APIMessage

import java.sql.Connection

final case class TryRecoveryFollowUpAPIMessage(gameId: String) extends APIMessage[Option[RecoveryFollowUp]]:
  override def plan(connection: Connection): IO[Option[RecoveryFollowUp]] =
    ReputationOperations.tryRecoveryFollowUp(connection, gameId)

object TryRecoveryFollowUpAPIMessage:
  given Codec.AsObject[TryRecoveryFollowUpAPIMessage] = deriveCodec
