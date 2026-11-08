// 康复回访只展示感谢信息，不会重新进入文字问诊。
package services.reputation.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class RecoveryFollowUp(
    patientName: String,
    message: String,
    reputationDelta: Double
)

object RecoveryFollowUp:
  given Codec.AsObject[RecoveryFollowUp] = deriveCodec
