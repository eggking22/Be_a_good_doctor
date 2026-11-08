// 游戏会话持久化记录：只对应 game_sessions 表的一行，不代表完整运行时聚合。
package services.game.objects

import io.circe.{Codec, Json, JsonObject}
import java.time.OffsetDateTime

final case class GameSessionRecord(
    id: String,
    playerName: String,
    currentDay: Int,
    currentMinute: Int,
    cash: BigDecimal,
    reputationScore: Double,
    bankrupt: Boolean,
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime
)

object GameSessionRecord:
  // 该类型主要用于数据库会话记录；保留伴生对象以集中放置序列化定义。
  given Codec.AsObject[GameSessionRecord] = Codec.AsObject.from(
    cursor =>
      for
        id <- cursor.get[String]("id")
        playerName <- cursor.get[String]("playerName")
        currentDay <- cursor.get[Int]("currentDay")
        currentMinute <- cursor.get[Option[Int]]("currentMinute")
        cash <- cursor.get[BigDecimal]("cash")
        reputationScore <- cursor.get[Double]("reputationScore")
        bankrupt <- cursor.get[Boolean]("bankrupt")
        createdAt <- cursor.get[String]("createdAt")
        updatedAt <- cursor.get[String]("updatedAt")
      yield GameSessionRecord(id, playerName, currentDay, currentMinute.getOrElse(8 * 60 + 30), cash, reputationScore, bankrupt, OffsetDateTime.parse(createdAt), OffsetDateTime.parse(updatedAt)),
    (value: GameSessionRecord) =>
      JsonObject.fromIterable(
        List(
          "id" -> Json.fromString(value.id),
          "playerName" -> Json.fromString(value.playerName),
          "currentDay" -> Json.fromInt(value.currentDay),
          "currentMinute" -> Json.fromInt(value.currentMinute),
          "cash" -> Json.fromBigDecimal(value.cash),
          "reputationScore" -> Json.fromDoubleOrNull(value.reputationScore),
          "bankrupt" -> Json.fromBoolean(value.bankrupt),
          "createdAt" -> Json.fromString(value.createdAt.toString),
          "updatedAt" -> Json.fromString(value.updatedAt.toString)
        )
      )
  )
