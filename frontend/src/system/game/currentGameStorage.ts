const currentGameStorageKey = 'ai-clinic-simulation.current-game-id'

export function getCurrentGameId(): string | null {
  const savedGameId = localStorage.getItem(currentGameStorageKey)
  if (savedGameId) {
    return savedGameId
  }

  // 兼容旧诊所页面使用的 sessionStorage，读取一次后迁移到长期存储。
  const legacyGameId = sessionStorage.getItem(currentGameStorageKey)
  if (legacyGameId) {
    localStorage.setItem(currentGameStorageKey, legacyGameId)
    sessionStorage.removeItem(currentGameStorageKey)
  }
  return legacyGameId
}

export function setCurrentGameId(gameId: string): void {
  localStorage.setItem(currentGameStorageKey, gameId)
}

export function clearCurrentGameId(): void {
  localStorage.removeItem(currentGameStorageKey)
  sessionStorage.removeItem(currentGameStorageKey)
}
