// AI 内部消息：调用 TokenHub 生成患者台词，并提取受限的玩家文字理解结果。
package services.ai.api

import cats.effect.IO
import services.ai.objects.{GeneratedPatientDialogue, PatientDialogueContext}
import services.ai.utils.{PromptManager, TokenHubChatClient}
import services.system.HttpError
import services.system.api.APIMessage

import java.sql.Connection

final case class GeneratePatientDialogueAPIMessage(
    context: PatientDialogueContext
) extends APIMessage[GeneratedPatientDialogue]:
  override def plan(connection: Connection): IO[GeneratedPatientDialogue] =
    for
      completion <- TokenHubChatClient.complete(PromptManager.systemPromptFor(context), PromptManager.patientPrompt(context))
      (content, modelName) = completion
      dialogue <- IO.fromEither(PromptManager.parseGeneratedDialogue(content, modelName).left.map { error =>
        HttpError.Conflict(s"患者话术格式无效，请重试：$error")
      })
    yield dialogue
