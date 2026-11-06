// 完整加载结果：GameState 保持会话级聚合，数据库集合以查询结果形式随加载返回。
package services.game.objects

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import services.consultation.objects.Consultation
import services.economy.objects.{DailyStatementRecord, LedgerTransaction}
import services.event.objects.GameEvent
import services.patient.objects.Patient
import services.reputation.objects.ReputationChange

final case class LoadedGameState(
    state: GameState,
    patients: List[Patient],
    consultations: List[Consultation],
    events: List[GameEvent],
    reputationHistory: List[ReputationChange],
    dailyStatements: List[DailyStatementRecord],
    ledgerTransactions: List[LedgerTransaction]
)

object LoadedGameState:
  given Codec.AsObject[LoadedGameState] = deriveCodec
