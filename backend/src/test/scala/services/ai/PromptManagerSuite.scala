package services.ai

import munit.FunSuite
import services.ai.objects.PatientDialogueContext
import services.ai.utils.PromptManager
import services.patient.objects.PatientEmotion

final class PromptManagerSuite extends FunSuite:
  private def context(difficulty: Int): PatientDialogueContext =
    PatientDialogueContext(
      patientName = "测试患者",
      communicationDifficulty = difficulty,
      emotion = PatientEmotion.Calm,
      symptomsAllowed = Nil,
      medicinesAllowed = Nil,
      recentMessages = Nil,
      playerText = None
    )

  test("不同沟通难度使用不同患者画像 Prompt") {
    assert(PromptManager.systemPromptFor(context(1)).contains("非常容易沟通"))
    assert(PromptManager.systemPromptFor(context(3)).contains("普通沟通难度"))
    assert(PromptManager.systemPromptFor(context(5)).contains("最难沟通"))
  }

  test("结束请求由患者画像、情绪和玩家态度综合判断") {
    val prompt = PromptManager.patientPrompt(context(5))
    assert(prompt.contains("不代表必须离开"))
    assert(prompt.contains("玩家态度、患者当前情绪、沟通难度"))
    assert(prompt.contains("完整的离场台词"))
    assert(prompt.contains("抱怨、冷淡告别后离开"))
  }
