// 疾病领域对象：描述药品适用的游戏内疾病，不承载治疗或数据库行为。
package services.pharmacy.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class Disease(
    id: String,
    name: String,
    attributes: DiseaseAttributes
)

object Disease:
  given Codec.AsObject[Disease] = deriveCodec
