// 语音适配对象：描述 ASR/TTS 所需的数据，不承担接诊和结算规则。
package services.voice.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class VoiceRequest(
    sessionId: String,
    language: String,
    audioFormat: String
)

object VoiceRequest:
  given Codec.AsObject[VoiceRequest] = deriveCodec
