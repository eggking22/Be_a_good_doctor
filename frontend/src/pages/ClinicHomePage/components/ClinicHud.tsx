import type { GameState } from '@/objects/game/GameState'

interface ClinicHudProps {
  game: GameState
  season: string
  gameTime: string
  isOpen: boolean
}

function formatMoney(value: number): string {
  return `¥${value.toLocaleString('zh-CN', { maximumFractionDigits: 0 })}`
}

export default function ClinicHud({ game, season, gameTime, isOpen }: ClinicHudProps) {
  const reputation = Math.max(0, Math.min(100, game.reputation.score))

  return (
    <header className="pointer-events-none absolute inset-x-0 top-0 z-30 flex items-start justify-between gap-4 p-4 sm:p-6">
      <section className="pointer-events-auto w-[min(19rem,calc(100vw-2rem))] rounded-2xl border border-[#f7e4ba]/80 bg-[#fff7e5]/90 p-4 text-[#31483f] shadow-[0_12px_35px_rgba(58,37,30,0.2)] backdrop-blur-md">
        <div className="flex items-start justify-between gap-3">
          <div>
            <p className="text-[0.65rem] font-bold uppercase tracking-[0.22em] text-[#9a6848]">诊所经营中</p>
            <h1 className="mt-1 text-lg font-black tracking-tight">黑心仁心诊所</h1>
          </div>
          <span className="rounded-full bg-[#ddc18a]/80 px-2 py-1 text-[0.65rem] font-bold text-[#5d4a31]">第 {game.currentDay} 天</span>
        </div>

        <div className="mt-4 grid grid-cols-2 gap-2">
          <div className="rounded-xl bg-[#efe1bd]/80 p-2.5">
            <p className="text-[0.65rem] font-semibold text-[#7c755f]">现金余额</p>
            <p className="mt-0.5 text-base font-black">{formatMoney(game.finance.cash)}</p>
          </div>
          <div className="rounded-xl bg-[#efe1bd]/80 p-2.5">
            <p className="text-[0.65rem] font-semibold text-[#7c755f]">今日收入</p>
            <p className="mt-0.5 text-base font-black text-[#4f7b68]">+{formatMoney(game.finance.cashFlow?.income ?? 0)}</p>
          </div>
        </div>

        <div className="mt-3">
          <div className="flex items-center justify-between text-xs font-bold">
            <span>诊所声誉</span>
            <span className="text-[#9a6848]">{reputation.toFixed(0)} / 100</span>
          </div>
          <div className="mt-1.5 h-2.5 overflow-hidden rounded-full bg-[#ded1ac]">
            <div className="h-full rounded-full bg-gradient-to-r from-[#b86f52] via-[#c79853] to-[#709776] transition-all duration-500" style={{ width: `${reputation}%` }} />
          </div>
        </div>
      </section>

      <section className="pointer-events-auto rounded-2xl border border-[#e4e4c5]/50 bg-[#34554d]/88 px-4 py-3 text-right text-[#fff8e8] shadow-[0_12px_35px_rgba(35,47,54,0.24)] backdrop-blur-md sm:px-5">
        <p className="text-xs font-semibold tracking-wide text-[#dfe7cf]">{season} · 春风营业日</p>
        <p className="mt-1 text-2xl font-black tracking-tight">{gameTime}</p>
        <div className="mt-2 flex items-center justify-end gap-2 text-xs font-bold">
          <span className={`h-2 w-2 rounded-full ${isOpen ? 'bg-[#a9d28f] shadow-[0_0_10px_#a9d28f]' : 'bg-[#db9a79]'}`} />
          {isOpen ? '正在营业' : '准备营业'}
        </div>
      </section>
    </header>
  )
}
