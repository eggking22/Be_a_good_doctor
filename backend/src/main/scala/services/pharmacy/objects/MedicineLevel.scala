// 药品分级：用于瓶签颜色、风险提示和后续库存/售药规则。
package services.pharmacy.objects

import io.circe.{Codec, Decoder, Encoder}

enum MedicineLevel:
  case Ordinary
  case EasyDrug
  case Controlled
  case HighAlert

  def displayName: String = this match
    case MedicineLevel.Ordinary => "普通药"
    case MedicineLevel.EasyDrug => "非处方药"
    case MedicineLevel.Controlled => "管制药物"
    case MedicineLevel.HighAlert => "高警示药物"

  def bottleLabel: String = this match
    case MedicineLevel.Ordinary => "白色瓶签"
    case MedicineLevel.EasyDrug => "绿色标签"
    case MedicineLevel.Controlled => "黑色瓶签"
    case MedicineLevel.HighAlert => "亮黄色瓶签"

object MedicineLevel:
  def fromString(value: String): Either[String, MedicineLevel] =
    if value.trim.equalsIgnoreCase("OverTheCounter") then Right(EasyDrug)
    else values.find(_.toString.equalsIgnoreCase(value.trim)).toRight(s"未知药品等级：$value")

  given Codec[MedicineLevel] = Codec.from(
    Decoder.decodeString.emap(fromString),
    Encoder.encodeString.contramap(_.toString)
  )
