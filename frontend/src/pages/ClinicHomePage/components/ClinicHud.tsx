import type { GameState } from '@/objects/game/GameState'
import { calendarForDay, formatGameTime, hasBusinessEnded, hasBusinessStarted } from '@/objects/game/GameCalendar'

interface ClinicHudProps {
  game: GameState
  isSettlingDay: boolean
  onSettleDay: () => Promise<void>
}

function formatMoney(value: number): string {
  return `¥${value.toLocaleString('zh-CN', { maximumFractionDigits: 0 })}`
}

export default function ClinicHud({ game, isSettlingDay, onSettleDay }: ClinicHudProps) {
  const reputation = Math.max(0, Math.min(100, game.reputation.score))
  const calendar = calendarForDay(game.currentDay)
  const businessEnded = hasBusinessEnded(game.currentMinute)
  const businessStarted = hasBusinessStarted(game.currentMinute)
  const businessLabel = businessEnded ? '营业结束' : businessStarted ? '正在营业' : '营业准备'

  return (
    <header className="pointer-events-none absolute inset-x-0 top-0 z-30 flex items-start justify-between gap-4 p-4 sm:p-6">
      <section className="pixel-panel pointer-events-auto w-[min(20rem,calc(100vw-2rem))] p-3 text-[#123b5f] sm:p-4">
        <div className="flex items-start justify-between gap-3">
          <div>
            <p className="text-[0.58rem] tracking-[0.12em] text-[#397ea8]">CLINIC ONLINE</p>
            <h1 className="mt-1 text-sm leading-6 text-[#103b60] sm:text-base">黑心仁心诊所</h1>
          </div>
          <span className="border-2 border-[#21618c] bg-[#a6e3ff] px-2 py-1 text-[0.6rem] text-[#103b60]">DAY {game.currentDay}</span>
        </div>

        <div className="mt-3 grid grid-cols-2 gap-2">
          <div className="pixel-card p-2">
            <p className="text-[0.58rem] text-[#467694]">现金余额</p>
            <p className="mt-1 text-xs text-[#113f63] sm:text-sm">{formatMoney(game.finance.cash)}</p>
          </div>
          <div className="pixel-card p-2">
            <p className="text-[0.58rem] text-[#467694]">今日收入</p>
            <p className="mt-1 text-xs text-[#176b92] sm:text-sm">+{formatMoney(game.finance.cashFlow?.income ?? 0)}</p>
          </div>
        </div>

        <div className="mt-3 border-t-2 border-dashed border-[#6ea4c4] pt-2">
          <div className="flex items-center justify-between text-[0.65rem]">
            <span>诊所声誉</span>
            <span className="text-[#1d6d99]">{reputation.toFixed(0)} / 100</span>
          </div>
          <div className="mt-1.5 h-3 overflow-hidden border-2 border-[#174d75] bg-[#bcdff0]">
            <div className="h-full bg-[#42a9d5] transition-[width] duration-500" style={{ width: `${reputation}%` }} />
          </div>
        </div>
      </section>

      <section className="pixel-dark-panel pointer-events-auto px-3 py-3 text-right text-[#f3fbff] sm:px-4">
          <p className="text-[0.6rem] tracking-[0.08em] text-[#a9e4ff]">第 {calendar.year} 年 · {calendar.season}第 {calendar.weekOfSeason} 周 · {calendar.weekday}</p>
          <p className="mt-2 text-xl leading-none sm:text-2xl">{formatGameTime(game.currentMinute)}</p>
        <div className="mt-3 flex items-center justify-end gap-2 text-[0.62rem]">
          <span className={`h-2.5 w-2.5 border border-[#dff8ff] ${businessEnded ? 'bg-[#f18b83] shadow-[2px_2px_0_#9d3a46]' : businessStarted ? 'bg-[#67e1a9] shadow-[2px_2px_0_#1a9463]' : 'bg-[#f5d66a] shadow-[2px_2px_0_#a17b2f]'}`} />
          {businessLabel}
        </div>
          {businessEnded && <button type="button" onClick={() => void onSettleDay()} disabled={isSettlingDay} className="pixel-button mt-3 w-full px-2 py-2 text-[0.58rem] disabled:cursor-not-allowed disabled:opacity-60">{isSettlingDay ? '正在结算…' : '营业结束 · 点击结算'}</button>}
      </section>
    </header>
  )
}
