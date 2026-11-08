// 完整读取存档时使用：将接诊、病例、文字分析和处方清单恢复为同一份快照。
package services.consultation.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class ConsultationSnapshot(
    consultation: Consultation,
    patientCase: Option[PatientCase],
    playerTextAnalyses: List[PlayerTextAnalysis],
    prescriptionItems: List[PrescriptionItem]
)

object ConsultationSnapshot:
  given Codec.AsObject[ConsultationSnapshot] = deriveCodec
