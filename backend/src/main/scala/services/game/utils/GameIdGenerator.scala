// 游戏 ID 工具：集中生成游戏会话 ID，避免 API 层重复编写 ID 规则。
package services.game.utils

import java.util.UUID

object GameIdGenerator:
  private[game] def nextId(): String = s"game-${UUID.randomUUID()}"
