package services.consultation

import munit.FunSuite
import services.ai.objects.GeneratedPatientDialogue
import services.consultation.objects.{DialogueMessage, PatientConversationAction}
import services.consultation.utils.{ConversationEndDetector, DialogueAIResultValidator, MedicineNameMatcher}
import services.pharmacy.objects.{Medicine, MedicinePrice}

final class ConversationRulesSuite extends FunSuite:
  private val coldMedicine = Medicine("medicine-cold", "感冒药", MedicinePrice(BigDecimal(10), BigDecimal(20)))
  private val fluMedicine = Medicine("medicine-flu", "流感对症药", MedicinePrice(BigDecimal(18), BigDecimal(36)))

  test("最多允许十个玩家回合") {
    val messages = List.fill(10)(DialogueMessage("player", "请描述一下", "2026-01-01T00:00:00Z"))
    assertEquals(ConversationEndDetector.playerTurnCount(messages), ConversationEndDetector.maximumPlayerTurns)
  }

  test("完整药品名会被后端兜底匹配") {
    assertEquals(MedicineNameMatcher.matchedMedicineIds("我给你开感冒药，按时吃。", List(coldMedicine, fluMedicine)), List("medicine-cold"))
    assertEquals(MedicineNameMatcher.matchedMedicineIds("先观察症状", List(coldMedicine, fluMedicine)), Nil)
  }

  test("已有购物清单时，后续未重复提到药名也允许患者离开") {
    val leaveDialogue = GeneratedPatientDialogue(
      text = "好的，谢谢医生，我先回去了。",
      mentionedSymptomIds = Nil,
      playerTextAnalysis = None,
      patientConversationAction = PatientConversationAction.Leave,
      shouldLeave = true,
      modelName = "test"
    )

    assertEquals(
      DialogueAIResultValidator.patientConversationAction(
        leaveDialogue,
        recognizedMedicineIds = Nil,
        hasExistingPrescription = true
      ),
      Right(PatientConversationAction.Leave)
    )
  }

  test("没有任何已识别药品时仍拒绝患者直接离开") {
    val leaveDialogue = GeneratedPatientDialogue(
      text = "好的，谢谢医生，我先回去了。",
      mentionedSymptomIds = Nil,
      playerTextAnalysis = None,
      patientConversationAction = PatientConversationAction.Leave,
      shouldLeave = true,
      modelName = "test"
    )

    assert(DialogueAIResultValidator.patientConversationAction(leaveDialogue, Nil).isLeft)
  }
