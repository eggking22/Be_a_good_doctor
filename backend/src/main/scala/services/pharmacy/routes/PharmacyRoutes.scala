// 药房服务注册：后续注册药品采购、库存和过期处理 API Message。
package services.pharmacy.routes

import services.system.api.ApiServiceMessages

object PharmacyRoutes:
  val apiMessages: ApiServiceMessages = ApiServiceMessages("pharmacy", Nil)
