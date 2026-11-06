// 声誉变化记录：与当前声誉状态分离，便于保存历史原因和时间。
package services.reputation.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class ReputationChange(
    amount: Double,
    reason: String,
    changedAt: String
)

object ReputationChange:
  given Codec.AsObject[ReputationChange] = deriveCodec
