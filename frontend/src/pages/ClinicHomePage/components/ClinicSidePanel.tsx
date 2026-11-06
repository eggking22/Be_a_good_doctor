import type { ClinicPanel } from '@/pages/ClinicHomePage/objects/ClinicHomeViewState'

interface ClinicSidePanelProps {
  panel: ClinicPanel
  isPatientPresent: boolean
  onClose: () => void
  onWelcomePatient: () => void
  onFinishConsultation: () => void
}

const panelContent: Record<ClinicPanel, { title: string; eyebrow: string; icon: string; description: string }> = {
  consultation: {
    title: '接诊台',
    eyebrow: 'PATIENT FLOW',
    icon: '🩺',
    description: '在这里查看当前接诊状态，并安排今天的问诊流程。',
  },
  pharmacy: {
    title: '药房',
    eyebrow: 'PHARMACY',
    icon: '💊',
    description: '查看药品库存和保存期限。正式药房页面将在后续阶段接入。',
  },
  staff: {
    title: '员工休息室',
    eyebrow: 'STAFF ROOM',
    icon: '🧑‍⚕️',
    description: '医生、护士和保安正在等待你的安排。员工管理页面暂未开放。',
  },
  finance: {
    title: '财务账本',
    eyebrow: 'LEDGER',
    icon: '🧾',
    description: '现金、收入和声誉变化会在这里汇总。当前先展示入口。',
  },
  expansion: {
    title: '扩建计划',
    eyebrow: 'UPGRADE',
    icon: '🔨',
    description: '准备扩建候诊区、药房和诊室，让更多患者排队等候。',
  },
}

export default function ClinicSidePanel({ panel, isPatientPresent, onClose, onWelcomePatient, onFinishConsultation }: ClinicSidePanelProps) {
  const content = panelContent[panel]

  return (
    <aside className="absolute inset-y-0 right-0 z-50 flex w-full max-w-sm flex-col border-l border-[#f1ddaf]/80 bg-[#fff9e9]/95 p-5 text-[#31483f] shadow-[-18px_0_45px_rgba(56,34,28,0.24)] backdrop-blur-xl sm:p-7">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-[0.65rem] font-black tracking-[0.2em] text-[#9a6848]">{content.eyebrow}</p>
          <h2 className="mt-1 flex items-center gap-2 text-2xl font-black tracking-tight"><span>{content.icon}</span>{content.title}</h2>
        </div>
        <button type="button" aria-label="关闭面板" onClick={onClose} className="rounded-full bg-[#ecdeb9] px-3 py-1 text-xl font-bold text-[#5c5947] transition hover:bg-[#ddc48f]">×</button>
      </div>

      <div className="mt-8 rounded-2xl border border-[#ead6a8] bg-[#fbf0d7] p-4 shadow-sm">
        <p className="text-sm font-semibold leading-6 text-[#695c4f]">{content.description}</p>
      </div>

      {panel === 'consultation' && (
        <div className="mt-5 rounded-2xl bg-[#f0e1be] p-4">
          <div className="flex items-center justify-between">
            <span className="text-sm font-black">当前诊室</span>
            <span className={`rounded-full px-2 py-1 text-[0.65rem] font-black ${isPatientPresent ? 'bg-[#d9e7c8] text-[#4d7557]' : 'bg-[#e6d4af] text-[#765f4c]'}`}>
              {isPatientPresent ? '患者在场' : '等待患者'}
            </span>
          </div>
          <p className="mt-2 text-xs leading-5 text-[#7e6c5c]">{isPatientPresent ? '已有患者等待接诊，可在此结束本次问诊。' : '当前尚未安排患者，可在此开始下一次接诊。'}</p>
          <div className="mt-4 flex gap-2">
            {!isPatientPresent && (
              <button type="button" onClick={onWelcomePatient} className="flex-1 rounded-xl bg-[#6c8b78] px-3 py-2.5 text-xs font-black text-[#fff9e9] shadow-[0_4px_0_#4b6b5d] transition hover:-translate-y-0.5">
                叫下一位患者
              </button>
            )}
            {isPatientPresent && (
              <button type="button" onClick={onFinishConsultation} className="flex-1 rounded-xl bg-[#55766a] px-3 py-2.5 text-xs font-black text-[#fff9e9] shadow-[0_4px_0_#3e594f] transition hover:-translate-y-0.5">
                送患者离开
              </button>
            )}
          </div>
        </div>
      )}

      {panel !== 'consultation' && (
        <div className="mt-auto rounded-2xl border border-dashed border-[#d9bd86] p-4 text-center text-xs font-bold text-[#937252]">功能页面待开发</div>
      )}
    </aside>
  )
}
