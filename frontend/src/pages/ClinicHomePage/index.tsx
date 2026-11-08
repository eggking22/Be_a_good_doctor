import { useState } from 'react'
import { useNavigate } from 'react-router-dom'

import BottomActionBar from '@/pages/ClinicHomePage/components/BottomActionBar'
import ClinicHud from '@/pages/ClinicHomePage/components/ClinicHud'
import ClinicRoomScene from '@/pages/ClinicHomePage/components/ClinicRoomScene'
import ClinicSidePanel from '@/pages/ClinicHomePage/components/ClinicSidePanel'
import PatientVisitOverlay from '@/pages/ClinicHomePage/components/PatientVisitOverlay'
import { useClinicHomePage } from '@/pages/ClinicHomePage/hooks/useClinicHomePage'

export default function ClinicHomePage() {
  const page = useClinicHomePage()
  const navigate = useNavigate()
  const [isSettingsOpen, setIsSettingsOpen] = useState(false)

  if (page.status.status === 'loading') {
    return <div className="pixel-font flex min-h-screen items-center justify-center bg-[#123b5f] p-6 text-center text-[#effaff]">正在连接诊所系统…</div>
  }

  if (page.status.status !== 'ready') {
    return (
      <div className="pixel-font flex min-h-screen flex-col items-center justify-center gap-4 bg-[#123b5f] p-6 text-center text-[#effaff]">
        <p>{page.status.message}</p>
        {page.status.status === 'error' && <button type="button" onClick={page.retryGame} className="pixel-button px-5 py-3 text-xs">重新连接</button>}
        <button type="button" onClick={() => navigate('/')} className="pixel-button px-5 py-3 text-xs">返回主菜单</button>
      </div>
    )
  }

  const { game } = page.status
  const isPatientPresent = page.interaction.patientStage === 'entering' || page.interaction.patientStage === 'consulting'

  return (
    <div className="pixel-font relative h-screen min-h-[42rem] overflow-hidden bg-[#123b5f] selection:bg-[#78d7ff] selection:text-[#08243c]">
      <ClinicRoomScene />
      <PatientVisitOverlay
        patient={page.interaction.patientPortrait}
        patientName={page.interaction.conversation?.patient.name ?? null}
        stage={page.interaction.patientStage}
        messages={page.interaction.conversation?.messages ?? []}
        isStarting={page.interaction.isStartingConsultation}
        isSendingText={page.interaction.isSendingText}
        isEnding={page.interaction.isEndingConsultation}
        shoppingList={page.interaction.conversation?.shoppingList ?? []}
        endReason={page.interaction.conversation?.endReason ?? null}
        error={page.interaction.consultationError}
        onSubmitText={page.submitPlayerText}
        onFinishConsultation={page.finishConsultation}
        onDismissEndedConsultation={page.dismissEndedConsultation}
      />
      <ClinicHud game={game} isSettlingDay={page.interaction.isSettlingDay} onSettleDay={page.settleDay} />

      <BottomActionBar activePanel={page.interaction.activePanel} onOpenPanel={page.openPanel} />

      <button type="button" onClick={() => setIsSettingsOpen(true)} className="pixel-button absolute bottom-4 left-4 z-50 px-3 py-2 text-[0.65rem] sm:bottom-6 sm:left-6">⚙ 设置</button>

      {page.interaction.activePanel && (
        <ClinicSidePanel
          panel={page.interaction.activePanel}
          isPatientPresent={isPatientPresent}
          patientName={page.interaction.conversation?.patient.name ?? null}
          isStartingConsultation={page.interaction.isStartingConsultation}
          error={page.interaction.consultationError}
          onClose={page.closePanel}
          onWelcomePatient={page.welcomePatient}
        />
      )}

      {isSettingsOpen && (
        <aside className="pixel-drawer absolute inset-y-0 left-0 z-[60] flex w-full max-w-xs flex-col p-5 text-[#123b5f] sm:p-7">
          <div className="flex items-start justify-between gap-4">
            <div><p className="text-[0.58rem] tracking-[0.16em] text-[#3d83ae]">CLINIC SETTINGS</p><h2 className="mt-2 text-xl">诊所设置</h2></div>
            <button type="button" aria-label="关闭设置" onClick={() => setIsSettingsOpen(false)} className="pixel-button px-3 py-1 text-base leading-none">×</button>
          </div>
          <div className="pixel-card mt-8 p-4 text-xs leading-6 text-[#275c7f]">当前操作都会即时写入数据库。退出到主菜单只离开诊室，不会删除或重置存档。</div>
          <button type="button" onClick={() => navigate('/')} className="pixel-button mt-5 px-4 py-3 text-xs">退出到主菜单</button>
        </aside>
      )}
    </div>
  )
}
