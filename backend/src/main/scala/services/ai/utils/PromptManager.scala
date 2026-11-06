// Prompt 工具：只负责组装患者对话提示词，游戏规则仍由 consultation/calculators 执行。
package services.ai.utils

import services.patient.objects.Patient

object PromptManager:
  private[ai] def patientPrompt(patient: Patient, conversation: String): String =
    s"""你正在扮演一名虚构患者。
       |患者类型：${patient.attributes.illnessDifficulty}
       |沟通难度：${patient.attributes.communicationDifficulty}
       |当前对话：$conversation
       |只生成角色表达，不直接决定游戏资产变化。""".stripMargin
