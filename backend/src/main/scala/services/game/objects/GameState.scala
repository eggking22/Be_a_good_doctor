// 游戏状态：保存一局经营游戏中需要持久化的核心经营指标。
package services.game.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import services.clinic.objects.Clinic
import services.economy.objects.FinanceState
import services.pharmacy.objects.Inventory
import services.reputation.objects.Reputation

final case class GameState(
    id: String,
    playerName: String,
    currentDay: Int,
    currentMinute: Int,
    finance: FinanceState,
    reputation: Reputation,
    clinic: Clinic,
    inventory: Inventory,
    bankrupt: Boolean,
    createdAt: String,
    updatedAt: String
)

object GameState:
  // API 返回游戏状态时使用的 JSON 编解码器。
  given Codec.AsObject[GameState] = deriveCodec
