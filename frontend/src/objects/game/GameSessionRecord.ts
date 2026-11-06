// 与后端 GameSessionRecord 对齐：数据库 game_sessions 表的一行。
// 后端 OffsetDateTime 通过 JSON 以 ISO 字符串传输，因此前端使用 string。
export interface GameSessionRecord {
  id: string
  playerName: string
  currentDay: number
  cash: number
  reputationScore: number
  bankrupt: boolean
  createdAt: string
  updatedAt: string
}
