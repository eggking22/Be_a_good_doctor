// 声誉领域对象：声誉是经营环境变量，不是生命值，范围固定在 0～100。
package services.reputation.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class Reputation(
    score: Double
)

object Reputation:
  given Codec.AsObject[Reputation] = deriveCodec
