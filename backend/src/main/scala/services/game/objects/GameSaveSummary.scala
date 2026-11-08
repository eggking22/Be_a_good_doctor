// 存档列表项：只返回主菜单展示需要的轻量信息，完整状态由 LoadGameAPIMessage 读取。
package services.game.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class GameSaveSummary(
    id: String,
    playerName: String,
    currentDay: Int,
    cash: BigDecimal,
    reputationScore: Double,
    bankrupt: Boolean,
    createdAt: String,
    updatedAt: String
)

object GameSaveSummary:
  def fromRecord(record: GameSessionRecord): GameSaveSummary =
    GameSaveSummary(
      id = record.id,
      playerName = record.playerName,
      currentDay = record.currentDay,
      cash = record.cash,
      reputationScore = record.reputationScore,
      bankrupt = record.bankrupt,
      createdAt = record.createdAt.toString,
      updatedAt = record.updatedAt.toString
    )

  given Codec.AsObject[GameSaveSummary] = deriveCodec
