// 药房采购 API：仅允许每日 08:30 的准备阶段进行采购。
package services.pharmacy.api

import cats.effect.IO
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import services.pharmacy.objects.PharmacyOverview
import services.pharmacy.utils.PharmacyOperations
import services.system.api.APIMessage

import java.sql.Connection

final case class PurchaseMedicineAPIMessage(
    gameId: String,
    medicineId: String,
    quantity: Int
) extends APIMessage[PharmacyOverview]:
  override def plan(connection: Connection): IO[PharmacyOverview] =
    PharmacyOperations.purchase(connection, gameId, medicineId, quantity)

object PurchaseMedicineAPIMessage:
  given Codec.AsObject[PurchaseMedicineAPIMessage] = deriveCodec
