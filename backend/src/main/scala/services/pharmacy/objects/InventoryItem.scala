// 库存项对象：把药品目录信息和某个库存批次的数量、有效期组合起来。
package services.pharmacy.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class InventoryItem(
    id: String,
    medicine: Medicine,
    quantity: Int,
    expiryDate: String
)

object InventoryItem:
  given Codec.AsObject[InventoryItem] = deriveCodec
