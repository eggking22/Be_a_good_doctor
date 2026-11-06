import type { EventRiskLevel } from '@/objects/event/EventRiskLevel'
import type { EventType } from '@/objects/event/EventType'

// 与后端 GameEvent 对齐：事件只描述状态和影响，规则由业务层执行。
export interface GameEvent {
  id: string
  eventType: EventType
  title: string
  description: string
  riskLevel: EventRiskLevel
  resolved: boolean
}
