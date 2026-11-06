// 库存聚合：组合一局游戏中的药品库存，不重复定义药品字段。
package services.pharmacy.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class Inventory(
    items: List[InventoryItem]
)

object Inventory:
  given Codec.AsObject[Inventory] = deriveCodec

  // 默认空库存只用于聚合初始化，真实药品由 InventoryTable 加载。
  def empty: Inventory = Inventory(items = Nil)
