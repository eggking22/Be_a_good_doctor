// 医疗目录导入结果：仅汇总本次写入的草稿内容数量。
package services.pharmacy.objects.apiTypes

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class MedicalCatalogImportResponse(
    diseaseCount: Int,
    symptomCount: Int,
    medicineCount: Int,
    diseaseSymptomCount: Int,
    diseaseMedicineCount: Int
)

object MedicalCatalogImportResponse:
  given Codec.AsObject[MedicalCatalogImportResponse] = deriveCodec
