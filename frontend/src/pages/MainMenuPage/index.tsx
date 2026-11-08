import { useCallback, useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'

import { ListGameSavesAPIMessage } from '@/apis/game/ListGameSavesAPIMessage'
import { LoadGameAPIMessage } from '@/apis/game/LoadGameAPIMessage'
import { StartGameAPIMessage } from '@/apis/game/StartGameAPIMessage'
import type { LoadedGameState } from '@/objects/game/LoadedGameState'
import type { GameSaveSummary } from '@/objects/game/GameSaveSummary'
import { clearCurrentGameId, getCurrentGameId, setCurrentGameId } from '@/system/game/currentGameStorage'
import { sendAPI } from '@/system/api/sendAPI'

type MenuPanel = 'load' | 'new' | 'history' | 'achievements' | null

const endings = [
  { title: '社区名医', description: '声誉达到 80，守住诊所招牌。', isUnlocked: (save: GameSaveSummary) => !save.bankrupt && save.reputationScore >= 80 },
  { title: '铁算盘', description: '现金达到 ¥12,000，经营稳步向前。', isUnlocked: (save: GameSaveSummary) => !save.bankrupt && save.cash >= 12000 },
  { title: '黑心资本家', description: '在低口碑中维持运转。', isUnlocked: (save: GameSaveSummary) => !save.bankrupt && save.reputationScore < 40 && save.cash >= 10000 },
  { title: '诊所覆灭', description: '资金链断裂，经营画上句号。', isUnlocked: (save: GameSaveSummary) => save.bankrupt },
]

export default function MainMenuPage() {
  const navigate = useNavigate()
  const [panel, setPanel] = useState<MenuPanel>(null)
  const [saves, setSaves] = useState<GameSaveSummary[]>([])
  const [selectedSaveId, setSelectedSaveId] = useState<string | null>(getCurrentGameId)
  const [history, setHistory] = useState<LoadedGameState | null>(null)
  const [playerName, setPlayerName] = useState('医生')
  const [isLoading, setIsLoading] = useState(true)
  const [isWorking, setIsWorking] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const selectedSave = useMemo(
    () => saves.find((save) => save.id === selectedSaveId) ?? null,
    [saves, selectedSaveId],
  )

  const refreshSaves = useCallback(async () => {
    setIsLoading(true)
    setError(null)
    try {
      const nextSaves = await sendAPI(new ListGameSavesAPIMessage())
      const storedGameId = getCurrentGameId()
      const usableStoredGameId = storedGameId && nextSaves.some((save) => save.id === storedGameId) ? storedGameId : null
      if (storedGameId && !usableStoredGameId) {
        clearCurrentGameId()
      }
      setSaves(nextSaves)
      setSelectedSaveId((currentId) => currentId && nextSaves.some((save) => save.id === currentId) ? currentId : usableStoredGameId ?? nextSaves[0]?.id ?? null)
    } catch (requestError) {
      setError(messageOf(requestError))
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    const refreshTimer = window.setTimeout(() => {
      void refreshSaves()
    }, 0)
    return () => window.clearTimeout(refreshTimer)
  }, [refreshSaves])

  async function loadSave(gameId: string) {
    setIsWorking(true)
    setError(null)
    try {
      await sendAPI(new LoadGameAPIMessage(gameId))
      setCurrentGameId(gameId)
      navigate('/game')
    } catch (requestError) {
      setError(messageOf(requestError))
      await refreshSaves()
    } finally {
      setIsWorking(false)
    }
  }

  async function startNewGame() {
    setIsWorking(true)
    setError(null)
    try {
      const game = await sendAPI(new StartGameAPIMessage(playerName.trim() || '医生', getCurrentGameId() ?? undefined))
      setCurrentGameId(game.id)
      navigate('/game')
    } catch (requestError) {
      setError(messageOf(requestError))
    } finally {
      setIsWorking(false)
    }
  }

  async function showHistory() {
    setPanel('history')
    setHistory(null)
    setError(null)
    const gameId = selectedSaveId ?? getCurrentGameId()
    if (!gameId) {
      return
    }
    setIsWorking(true)
    try {
      setHistory(await sendAPI(new LoadGameAPIMessage(gameId)))
    } catch (requestError) {
      setError(messageOf(requestError))
    } finally {
      setIsWorking(false)
    }
  }

  return (
    <main className="pixel-font min-h-screen overflow-x-hidden bg-[#071d31] text-[#eaf8ff]" style={{ backgroundImage: "linear-gradient(rgba(4, 22, 39, 0.86), rgba(4, 22, 39, 0.95)), url('/clinic-room-pixel-main.png')", backgroundPosition: 'center', backgroundSize: 'cover' }}>
      <div className="mx-auto flex min-h-screen w-full max-w-6xl flex-col px-5 py-8 sm:px-10 sm:py-12">
        <header className="max-w-3xl pt-4 sm:pt-10">
          <p className="text-[0.65rem] tracking-[0.32em] text-[#88dfff]">AI CLINIC MANAGEMENT SIMULATION</p>
          <h1 className="mt-5 text-4xl leading-tight text-[#ffffff] drop-shadow-[5px_5px_0_#062039] sm:text-6xl">黑心诊所模拟器</h1>
          <p className="mt-5 max-w-2xl text-sm leading-7 text-[#c2eaff]">在诊疗、现金与声誉之间维持一家摇摇欲坠的诊所。每一份存档，都记录一次经营选择。</p>
        </header>

        <section className="mt-10 grid max-w-3xl gap-3 sm:grid-cols-2">
          <MenuButton label="载入存档" hint="继续已有诊所" icon="▶" onClick={() => setPanel('load')} />
          <MenuButton label="开始新游戏" hint="替换当前存档" icon="+" onClick={() => setPanel('new')} />
          <MenuButton label="查看历史记录" hint="日结算与声誉变化" icon="≡" onClick={() => void showHistory()} />
          <MenuButton label="成就与结局" hint="收集不同经营结局" icon="★" onClick={() => setPanel('achievements')} />
        </section>

        <section className="pixel-panel mt-6 max-w-3xl p-4 text-[#123b5f] sm:p-6">
          {isLoading && <p className="text-sm">正在读取本机存档…</p>}
          {!isLoading && !panel && <p className="text-sm leading-6">{selectedSave ? `当前存档：${selectedSave.playerName} · 第 ${selectedSave.currentDay} 天` : '尚未选择存档。开始新游戏后会自动创建第一份存档。'}</p>}
          {!isLoading && panel === 'load' && (
            <SaveList saves={saves} selectedSaveId={selectedSaveId} isWorking={isWorking} onSelect={setSelectedSaveId} onLoad={loadSave} />
          )}
          {!isLoading && panel === 'new' && (
            <div>
              <h2 className="text-lg">新建诊所</h2>
              <p className="mt-2 text-xs leading-5 text-[#47708b]">新游戏会在同一事务中删除当前指针所指向的旧存档，再创建新的初始诊所；其他存档不会被删除。</p>
              <label className="mt-5 block text-xs" htmlFor="player-name">医生称呼</label>
              <input id="player-name" value={playerName} maxLength={30} onChange={(event) => setPlayerName(event.target.value)} className="mt-2 w-full border-2 border-[#245b82] bg-[#f6fcff] px-3 py-2 text-sm outline-none focus:border-[#1a8fc4]" />
              <button type="button" onClick={() => void startNewGame()} disabled={isWorking} className="pixel-button mt-4 px-4 py-2 text-xs disabled:cursor-not-allowed disabled:opacity-60">{isWorking ? '正在建立新诊所…' : '确认开始新游戏'}</button>
            </div>
          )}
          {!isLoading && panel === 'history' && <HistoryPanel history={history} hasSelectedSave={Boolean(selectedSaveId)} isWorking={isWorking} />}
          {!isLoading && panel === 'achievements' && <AchievementPanel saves={saves} />}
          {error && <p role="alert" className="mt-4 border-2 border-[#a64e4e] bg-[#ffe2dc] p-3 text-xs leading-5 text-[#8d3434]">{error}</p>}
        </section>

        <footer className="mt-auto pt-10 text-[0.62rem] tracking-wide text-[#9acde5]">存档会在每次后端操作时写入数据库；退出到主菜单不会删除当前存档。</footer>
      </div>
    </main>
  )
}

function MenuButton({ label, hint, icon, onClick }: { label: string; hint: string; icon: string; onClick: () => void }) {
  return (
    <button type="button" onClick={onClick} className="pixel-action flex min-h-24 items-center gap-4 p-4 text-left text-[#123b5f] sm:min-h-28 sm:p-5">
      <span className="pixel-icon text-xl"><span>{icon}</span></span>
      <span><span className="block text-base">{label}</span><span className="mt-1 block text-[0.68rem] text-[#52809d]">{hint}</span></span>
    </button>
  )
}

function SaveList({ saves, selectedSaveId, isWorking, onSelect, onLoad }: { saves: GameSaveSummary[]; selectedSaveId: string | null; isWorking: boolean; onSelect: (gameId: string) => void; onLoad: (gameId: string) => Promise<void> }) {
  if (saves.length === 0) {
    return <p className="text-sm leading-6">没有可载入的存档。请先开始一局新游戏。</p>
  }

  return (
    <div>
      <h2 className="text-lg">载入存档</h2>
      <div className="mt-4 grid gap-3">
        {saves.map((save) => (
          <button key={save.id} type="button" disabled={isWorking} onClick={() => { onSelect(save.id); void onLoad(save.id) }} className={`border-2 p-3 text-left shadow-[3px_3px_0_#173d5d] disabled:opacity-60 ${selectedSaveId === save.id ? 'border-[#176b92] bg-[#c6efff]' : 'border-[#5f92af] bg-[#effaff]'}`}>
            <span className="flex items-center justify-between gap-3 text-sm"><span>{save.playerName}的诊所</span><span>{save.bankrupt ? '已破产' : `第 ${save.currentDay} 天`}</span></span>
            <span className="mt-2 block text-[0.66rem] text-[#49718a]">现金 ¥{save.cash.toLocaleString('zh-CN')} · 声誉 {save.reputationScore.toFixed(0)} · {formatDate(save.updatedAt)}</span>
          </button>
        ))}
      </div>
    </div>
  )
}

function HistoryPanel({ history, hasSelectedSave, isWorking }: { history: LoadedGameState | null; hasSelectedSave: boolean; isWorking: boolean }) {
  if (!hasSelectedSave) {
    return <p className="text-sm leading-6">选择或创建存档后，才能查看经营历史。</p>
  }
  if (isWorking || !history) {
    return <p className="text-sm">正在读取历史记录…</p>
  }
  return (
    <div>
      <h2 className="text-lg">经营历史</h2>
      <p className="mt-2 text-xs text-[#47708b]">第 {history.state.currentDay} 天 · 共 {history.consultationSnapshots.length} 次接诊 · {history.patients.length} 位患者记录</p>
      <div className="mt-4 grid gap-3 sm:grid-cols-2">
        <HistoryCard title="最近日结算">
          {history.dailyStatements.length === 0 ? <p>尚无日结算。</p> : history.dailyStatements.slice(-5).reverse().map((item) => <p key={item.id}>第 {item.day} 天：¥{item.statement.openingCash} → ¥{item.statement.closingCash}</p>)}
        </HistoryCard>
        <HistoryCard title="声誉变化">
          {history.reputationHistory.length === 0 ? <p>尚无声誉记录。</p> : history.reputationHistory.slice(-5).reverse().map((item) => <p key={`${item.changedAt}-${item.reason}`}>{item.amount >= 0 ? '+' : ''}{item.amount.toFixed(1)} · {item.reason}</p>)}
        </HistoryCard>
      </div>
    </div>
  )
}

function HistoryCard({ title, children }: { title: string; children: React.ReactNode }) {
  return <section className="border-2 border-[#5f92af] bg-[#effaff] p-3 text-[0.68rem] leading-6"><h3 className="text-xs text-[#174d75]">{title}</h3><div className="mt-2 text-[#426b84]">{children}</div></section>
}

function AchievementPanel({ saves }: { saves: GameSaveSummary[] }) {
  return (
    <div>
      <h2 className="text-lg">结局收集册</h2>
      <p className="mt-2 text-xs leading-5 text-[#47708b]">这里预览不同经营结局的解锁条件；正式结局将在游戏结算系统接入后固定记录。</p>
      <div className="mt-4 grid gap-3 sm:grid-cols-2">
        {endings.map((ending) => {
          const unlocked = saves.some(ending.isUnlocked)
          return <article key={ending.title} className={`border-2 p-3 shadow-[3px_3px_0_#173d5d] ${unlocked ? 'border-[#2d876a] bg-[#d7f8e8]' : 'border-[#6d8ea1] bg-[#e5edf1]'}`}><p className="text-sm">{unlocked ? '★' : '×'} {ending.title}</p><p className="mt-2 text-[0.66rem] leading-5 text-[#4c7288]">{ending.description}</p><p className="mt-2 text-[0.58rem]">{unlocked ? '已解锁' : '尚未解锁'}</p></article>
        })}
      </div>
    </div>
  )
}

function formatDate(value: string): string {
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString('zh-CN', { dateStyle: 'medium', timeStyle: 'short' })
}

function messageOf(error: unknown): string {
  return error instanceof Error ? error.message : '请求失败，请稍后重试。'
}
