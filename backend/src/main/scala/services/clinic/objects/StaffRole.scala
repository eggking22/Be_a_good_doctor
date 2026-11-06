// 员工角色枚举：固定角色不使用 String，避免出现无法处理的未知角色。
package services.clinic.objects

import io.circe.{Codec, Decoder, Encoder}

enum StaffRole:
  case Doctor
  case Nurse
  case Receptionist
  case SecurityGuard
  case AIAssistant

object StaffRole:
  def fromString(value: String): Either[String, StaffRole] =
    values.find(_.toString.equalsIgnoreCase(value.trim)).toRight(s"未知员工角色：$value")

  given Codec[StaffRole] = Codec.from(
    Decoder.decodeString.emap(fromString),
    Encoder.encodeString.contramap(_.toString)
  )
