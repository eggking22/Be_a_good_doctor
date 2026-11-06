// 语音识别结果对象：保存 ASR 输出文本和识别置信度。
package services.voice.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class VoiceText(
    text: String,
    confidence: Double
)

object VoiceText:
  given Codec.AsObject[VoiceText] = deriveCodec
