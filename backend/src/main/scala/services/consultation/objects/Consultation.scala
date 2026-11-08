// 接诊领域对象：只描述一次接诊过程，不保存 gameId；所属游戏由 ConsultationTable 的外键关联。
package services.consultation.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import services.patient.objects.{Patient, PatientEmotion}

final case class Consultation(
    id: String,
    patient: Patient,
    emotion: PatientEmotion,
    status: ConsultationStatus,
    dialogueHistory: List[DialogueMessage],
    dialogueCount: Int,
    treatmentEvaluation: Option[TreatmentEvaluation],
    cashChange: BigDecimal,
    reputationChange: Double,
    caseId: Option[String] = None,
    settled: Boolean = false,
    closedDay: Option[Int] = None,
    followUpCompleted: Boolean = false
)

object Consultation:
  given Codec.AsObject[Consultation] = deriveCodec
