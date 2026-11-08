// 药房服务注册：公开医疗目录读取接口；目录导入仅供后端维护命令使用。
package services.pharmacy.routes

import io.circe.generic.auto.*
import services.pharmacy.api.{GetMedicalCatalogAPIMessage, GetPharmacyOverviewAPIMessage, PurchaseMedicineAPIMessage}
import services.pharmacy.objects.PharmacyOverview
import services.pharmacy.objects.apiTypes.MedicalCatalogResponse
import services.system.api.ApiServiceMessages
import services.system.api.RegisteredAPIMessage

object PharmacyRoutes:
  val apiMessages: ApiServiceMessages = ApiServiceMessages(
    "pharmacy",
    List(
      RegisteredAPIMessage.query[GetMedicalCatalogAPIMessage, MedicalCatalogResponse],
      RegisteredAPIMessage.query[GetPharmacyOverviewAPIMessage, PharmacyOverview],
      RegisteredAPIMessage.api[PurchaseMedicineAPIMessage, PharmacyOverview]
    )
  )
