// 库存计算器：只处理库存增加、消耗和过期判断，不负责持久化。
package services.pharmacy.calculators

object InventoryCalculator:
  private[pharmacy] def purchase(currentStock: Int, quantity: Int): Int =
    currentStock + quantity.max(0)

  private[pharmacy] def consume(currentStock: Int, quantity: Int): Option[Int] =
    if quantity <= 0 || quantity > currentStock then None else Some(currentStock - quantity)

  private[pharmacy] def isExpired(expiryDate: String, today: String): Boolean = expiryDate < today
