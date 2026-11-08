// 医疗目录读取：供前端验证和后续药房界面读取已导入的疾病、症状、药物及关系。
package services.pharmacy.api

import cats.effect.IO
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import services.pharmacy.objects.apiTypes.MedicalCatalogResponse
import services.pharmacy.tables.{DiseaseMedicineTable, DiseaseSymptomTable, DiseaseTable, MedicineTable, SymptomTable}
import services.system.api.APIMessage

import java.sql.Connection

final case class GetMedicalCatalogAPIMessage() extends APIMessage[MedicalCatalogResponse]:
  override def plan(connection: Connection): IO[MedicalCatalogResponse] =
    for
      diseases <- DiseaseTable.findAll(connection)
      symptoms <- SymptomTable.findAll(connection)
      medicines <- MedicineTable.findAll(connection)
      diseaseSymptoms <- DiseaseSymptomTable.findAll(connection)
      diseaseMedicines <- DiseaseMedicineTable.findAll(connection)
    yield MedicalCatalogResponse(diseases, symptoms, medicines, diseaseSymptoms, diseaseMedicines)

object GetMedicalCatalogAPIMessage:
  given Codec.AsObject[GetMedicalCatalogAPIMessage] = deriveCodec
