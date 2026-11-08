// 本次接诊购物清单项：只记录已从玩家文字中识别并经目录校验的药品。
package services.consultation.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import services.pharmacy.objects.Medicine

final case class PrescriptionItem(
    medicine: Medicine,
    quantity: Int
)

object PrescriptionItem:
  given Codec.AsObject[PrescriptionItem] = deriveCodec
