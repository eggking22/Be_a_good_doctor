// 主菜单存档列表使用的轻量摘要；完整内容通过 LoadGameAPIMessage 单独读取。
export interface GameSaveSummary {
  id: string
  playerName: string
  currentDay: number
  cash: number
  reputationScore: number
  bankrupt: boolean
  createdAt: string
  updatedAt: string
}
