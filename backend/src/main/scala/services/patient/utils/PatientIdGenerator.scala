// 患者 ID 工具：集中生成患者实体 ID。
package services.patient.utils

import java.util.UUID

object PatientIdGenerator:
  private[patient] def nextId(): String = s"patient-${UUID.randomUUID()}"
