// 诊所聚合：组合设施和员工，安保能力与员工满意度由 calculators 推导。
package services.clinic.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class Clinic(
    facilities: List[Facility],
    employees: List[Staff]
)

object Clinic:
  given Codec.AsObject[Clinic] = deriveCodec

  // 默认空诊所只用于聚合初始化，真实员工和设施由 StaffTable、FacilityTable 加载。
  def empty: Clinic = Clinic(facilities = Nil, employees = Nil)
