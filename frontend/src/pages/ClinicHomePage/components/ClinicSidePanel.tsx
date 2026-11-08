import type { ClinicPanel } from '@/pages/ClinicHomePage/objects/ClinicHomeViewState'

interface ClinicSidePanelProps {
  panel: ClinicPanel
  isPatientPresent: boolean
  patientName: string | null
  isStartingConsultation: boolean
  error: string | null
  onClose: () => void
  onWelcomePatient: () => Promise<void>
}

const panelContent: Record<ClinicPanel, { title: string; eyebrow: string; icon: string; description: string }> = {
  consultation: {
    title: '接诊台',
    eyebrow: 'PATIENT FLOW',
    icon: '+',
    description: '在这里查看当前接诊状态，并安排今天的问诊流程。',
  },
  pharmacy: {
    title: '药房',
    eyebrow: 'PHARMACY',
    icon: 'Rx',
    description: '查看药品库存和保存期限。正式药房页面将在后续阶段接入。',
  },
  staff: {
    title: '员工休息室',
    eyebrow: 'STAFF ROOM',
    icon: 'MD',
    description: '医生、护士和保安正在等待你的安排。员工管理页面暂未开放。',
  },
  finance: {
    title: '财务账本',
    eyebrow: 'LEDGER',
    icon: '¥',
    description: '现金、收入和声誉变化会在这里汇总。当前先展示入口。',
  },
  expansion: {
    title: '扩建计划',
    eyebrow: 'UPGRADE',
    icon: 'UP',
    description: '准备扩建候诊区、药房和诊室，让更多患者排队等候。',
  },
}

export default function ClinicSidePanel({ panel, isPatientPresent, patientName, isStartingConsultation, error, onClose, onWelcomePatient }: ClinicSidePanelProps) {
  const content = panelContent[panel]

  return (
    <aside className="pixel-drawer absolute inset-y-0 right-0 z-50 flex w-full max-w-sm flex-col p-5 text-[#123b5f] sm:p-7">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-[0.58rem] tracking-[0.16em] text-[#3d83ae]">{content.eyebrow}</p>
          <h2 className="mt-2 flex items-center gap-2 text-xl leading-7 sm:text-2xl"><span className="pixel-icon"><span>{content.icon}</span></span>{content.title}</h2>
        </div>
        <button type="button" aria-label="关闭面板" onClick={onClose} className="pixel-button px-3 py-1 text-base leading-none">×</button>
      </div>

      <div className="pixel-card mt-8 p-4">
        <p className="text-xs leading-6 text-[#275c7f]">{content.description}</p>
      </div>

      {panel === 'consultation' && (
        <div className="pixel-card mt-5 bg-[#d8f2ff] p-4">
          <div className="flex items-center justify-between">
            <span className="text-xs">当前诊室</span>
            <span className={`border-2 px-2 py-1 text-[0.58rem] ${isPatientPresent ? 'border-[#217e68] bg-[#bdf5db] text-[#176b59]' : 'border-[#6b8da4] bg-[#d5e5ed] text-[#3c6078]'}`}>
              {isPatientPresent ? '患者在场' : '等待患者'}
            </span>
          </div>
          <p className="mt-3 text-[0.68rem] leading-5 text-[#3d6985]">{isPatientPresent ? `${patientName ?? '患者'}正在诊室中，可在主场景进行文字问诊。` : '当前尚未安排患者，可在此开始下一次接诊。'}</p>
          <div className="mt-4 flex gap-2">
            {!isPatientPresent && (
              <button type="button" onClick={() => void onWelcomePatient()} disabled={isStartingConsultation} className="pixel-button flex-1 px-3 py-2.5 text-[0.68rem] disabled:cursor-not-allowed disabled:opacity-50">
                {isStartingConsultation ? '正在接诊…' : '叫下一位患者'}
              </button>
            )}
            {isPatientPresent && (
              <div className="w-full border-2 border-dashed border-[#4b88aa] bg-[#effaff] p-3 text-center text-[0.62rem] text-[#2d688b]">文字问诊已接入；结算与结束接诊将在后续阶段开放。</div>
            )}
          </div>
          {error && <p role="alert" className="mt-3 border-2 border-[#a64e4e] bg-[#ffe2dc] p-2 text-[0.58rem] leading-4 text-[#8d3434]">{error}</p>}
        </div>
      )}

      {panel !== 'consultation' && (
        <div className="mt-auto border-2 border-dashed border-[#4783a8] bg-[#dff3ff] p-4 text-center text-[0.68rem] text-[#2c6489]">功能页面待开发</div>
      )}
    </aside>
  )
}
