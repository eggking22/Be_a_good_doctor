// 接诊 ID 工具：集中生成接诊记录编号。
package services.consultation.utils

import java.util.UUID

object ConsultationIdGenerator:
  private[consultation] def nextId(): String = s"consultation-${UUID.randomUUID()}"
