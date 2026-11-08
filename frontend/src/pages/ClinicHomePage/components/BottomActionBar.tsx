import type { ClinicPanel } from '@/pages/ClinicHomePage/objects/ClinicHomeViewState'

interface BottomActionBarProps {
  activePanel: ClinicPanel | null
  onOpenPanel: (panel: ClinicPanel) => void
}

const actions: Array<{ panel: ClinicPanel; icon: string; label: string; hint: string }> = [
  { panel: 'consultation', icon: '+', label: '接诊', hint: '迎接患者' },
  { panel: 'pharmacy', icon: 'Rx', label: '药房', hint: '库存管理' },
  { panel: 'staff', icon: 'MD', label: '员工', hint: '查看团队' },
  { panel: 'finance', icon: '¥', label: '财务', hint: '账本与现金' },
  { panel: 'expansion', icon: 'UP', label: '扩建', hint: '升级诊所' },
]

export default function BottomActionBar({ activePanel, onOpenPanel }: BottomActionBarProps) {
  return (
    <nav className="absolute inset-x-0 bottom-0 z-40 flex justify-center px-3 pb-3 sm:pb-5">
      <div className="pixel-panel grid w-full max-w-3xl grid-cols-5 gap-1.5 p-2 sm:gap-2 sm:p-3">
        {actions.map((action) => {
          const isActive = activePanel === action.panel
          return (
            <button
              key={action.panel}
              type="button"
              onClick={() => onOpenPanel(action.panel)}
              className={`pixel-action group flex min-h-14 flex-col items-center justify-center px-1 py-1.5 text-[#16476d] sm:min-h-16 ${isActive ? 'pixel-action--active' : ''}`}
            >
              <span className="pixel-icon text-xl leading-none sm:text-2xl"><span>{action.icon}</span></span>
              <span className="mt-1 text-[0.68rem] sm:text-xs">{action.label}</span>
              <span className={`hidden text-[0.52rem] sm:block ${isActive ? 'text-[#d9f6ff]' : 'text-[#5b86a2]'}`}>{action.hint}</span>
            </button>
          )
        })}
      </div>
    </nav>
  )
}
