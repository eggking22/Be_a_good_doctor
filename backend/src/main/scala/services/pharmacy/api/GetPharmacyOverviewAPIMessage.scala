// 药房读取 API：供诊所药房面板显示库存和市场药品。
package services.pharmacy.api

import cats.effect.IO
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import services.pharmacy.objects.PharmacyOverview
import services.pharmacy.utils.PharmacyOperations
import services.system.api.APIMessage

import java.sql.Connection

final case class GetPharmacyOverviewAPIMessage(gameId: String) extends APIMessage[PharmacyOverview]:
  override def plan(connection: Connection): IO[PharmacyOverview] =
    PharmacyOperations.overview(connection, gameId)

object GetPharmacyOverviewAPIMessage:
  given Codec.AsObject[GetPharmacyOverviewAPIMessage] = deriveCodec
