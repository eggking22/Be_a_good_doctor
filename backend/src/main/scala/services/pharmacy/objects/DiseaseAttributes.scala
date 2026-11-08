// 疾病属性：难度用于匹配患者病情，抽取概率用于同难度疾病池内的加权选择。
package services.pharmacy.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class DiseaseAttributes(
    difficulty: Int,
    extractionProbability: Double
)

object DiseaseAttributes:
  given Codec.AsObject[DiseaseAttributes] = deriveCodec
