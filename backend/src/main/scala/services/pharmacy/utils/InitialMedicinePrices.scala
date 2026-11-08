// 示例药品的初始价格集中在此处，修改仅影响尚未写入数据库的初始药品。
package services.pharmacy.utils

import services.pharmacy.objects.MedicinePrice

private[pharmacy] object InitialMedicinePrices:
  val coldMedicine: MedicinePrice = MedicinePrice(
    purchasePrice = BigDecimal("10.00"),
    salePrice = BigDecimal("20.00")
  )

  val fluMedicine: MedicinePrice = MedicinePrice(
    purchasePrice = BigDecimal("18.00"),
    salePrice = BigDecimal("36.00")
  )

  val respiratoryMedicine: MedicinePrice = MedicinePrice(
    purchasePrice = BigDecimal("45.00"),
    salePrice = BigDecimal("90.00")
  )
