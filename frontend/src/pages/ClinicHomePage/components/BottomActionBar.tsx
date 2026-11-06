import type { ClinicPanel } from '@/pages/ClinicHomePage/objects/ClinicHomeViewState'

interface BottomActionBarProps {
  activePanel: ClinicPanel | null
  onOpenPanel: (panel: ClinicPanel) => void
}

const actions: Array<{ panel: ClinicPanel; icon: string; label: string; hint: string }> = [
  { panel: 'consultation', icon: '🩺', label: '接诊', hint: '迎接患者' },
  { panel: 'pharmacy', icon: '💊', label: '药房', hint: '库存管理' },
  { panel: 'staff', icon: '🧑‍⚕️', label: '员工', hint: '查看团队' },
  { panel: 'finance', icon: '🧾', label: '财务', hint: '账本与现金' },
  { panel: 'expansion', icon: '🔨', label: '扩建', hint: '升级诊所' },
]

export default function BottomActionBar({ activePanel, onOpenPanel }: BottomActionBarProps) {
  return (
    <nav className="absolute inset-x-0 bottom-0 z-40 flex justify-center px-3 pb-3 sm:pb-5">
      <div className="grid w-full max-w-3xl grid-cols-5 gap-1.5 rounded-[1.4rem] border border-[#f6e2b8]/80 bg-[#fff8e7]/92 p-2 shadow-[0_12px_35px_rgba(58,37,30,0.27)] backdrop-blur-md sm:gap-2 sm:p-3">
        {actions.map((action) => {
          const isActive = activePanel === action.panel
          return (
            <button
              key={action.panel}
              type="button"
              onClick={() => onOpenPanel(action.panel)}
              className={`group flex min-h-14 flex-col items-center justify-center rounded-xl px-1 py-1.5 transition-all duration-200 sm:min-h-16 ${isActive ? 'bg-[#5e8273] text-[#fff9e9] shadow-[0_5px_0_#426257]' : 'text-[#42564b] hover:-translate-y-1 hover:bg-[#e0e7c9]'}`}
            >
              <span className="text-xl leading-none transition-transform group-hover:scale-110 sm:text-2xl">{action.icon}</span>
              <span className="mt-1 text-[0.68rem] font-black sm:text-xs">{action.label}</span>
              <span className={`hidden text-[0.55rem] font-semibold sm:block ${isActive ? 'text-[#fff9e9]/80' : 'text-[#7d8065]'}`}>{action.hint}</span>
            </button>
          )
        })}
      </div>
    </nav>
  )
}
