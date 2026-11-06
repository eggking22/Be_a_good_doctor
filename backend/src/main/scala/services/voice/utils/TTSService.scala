// 语音合成适配接口：具体云服务或本地模型以后接入。
package services.voice.utils

import cats.effect.IO

private[voice] trait TTSService:
  def synthesize(text: String, language: String): IO[Array[Byte]]
