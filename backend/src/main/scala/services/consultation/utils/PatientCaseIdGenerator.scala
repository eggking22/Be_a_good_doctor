// 病例 ID 工具：集中生成病例编号。
package services.consultation.utils

import java.util.UUID

object PatientCaseIdGenerator:
  private[consultation] def nextId(): String = s"case-${UUID.randomUUID()}"
