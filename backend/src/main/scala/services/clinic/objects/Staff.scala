// 员工领域对象：工资、能力和满意度由诊所服务管理，安保能力由计算器汇总。
package services.clinic.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class Staff(
    id: String,
    name: String,
    role: StaffRole,
    dailySalary: BigDecimal,
    ability: Int,
    communicationAbility: Int,
    satisfaction: Double,
    active: Boolean
)

object Staff:
  given Codec.AsObject[Staff] = deriveCodec
