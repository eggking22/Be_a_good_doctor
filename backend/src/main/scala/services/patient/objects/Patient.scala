// 患者领域对象：病例真实属性由游戏规则生成，AI 只负责表达和对话。
package services.patient.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class Patient(
    id: String,
    name: String,
    // 患者的四个经营属性集中在 PatientAttributes，便于独立生成和校验。
    attributes: PatientAttributes,
    // 初始情绪在患者生成时确定，并在接诊开始前保持可查询。
    initialEmotion: PatientEmotion
)

object Patient:
  given Codec.AsObject[Patient] = deriveCodec

