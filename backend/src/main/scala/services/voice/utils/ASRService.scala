// 语音识别适配接口：具体云服务或本地模型以后接入。
package services.voice.utils

import cats.effect.IO
import services.voice.objects.{VoiceRequest, VoiceText}

private[voice] trait ASRService:
  def recognize(request: VoiceRequest, audioBytes: Array[Byte]): IO[VoiceText]
