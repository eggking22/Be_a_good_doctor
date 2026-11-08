import BottomActionBar from '@/pages/ClinicHomePage/components/BottomActionBar'
import ClinicHud from '@/pages/ClinicHomePage/components/ClinicHud'
import ClinicRoomScene from '@/pages/ClinicHomePage/components/ClinicRoomScene'
import ClinicSidePanel from '@/pages/ClinicHomePage/components/ClinicSidePanel'
import PatientVisitOverlay from '@/pages/ClinicHomePage/components/PatientVisitOverlay'
import { useClinicHomePage } from '@/pages/ClinicHomePage/hooks/useClinicHomePage'

export default function ClinicHomePage() {
  const page = useClinicHomePage()

  if (page.status.status === 'loading') {
    return <div className="pixel-font flex min-h-screen items-center justify-center bg-[#123b5f] p-6 text-center text-[#effaff]">正在连接诊所系统…</div>
  }

  if (page.status.status !== 'ready') {
    return (
      <div className="pixel-font flex min-h-screen flex-col items-center justify-center gap-4 bg-[#123b5f] p-6 text-center text-[#effaff]">
        <p>{page.status.message}</p>
        <button type="button" onClick={page.retryGame} className="pixel-button px-5 py-3 text-xs">重新连接</button>
      </div>
    )
  }

  const { game, season, gameTime, isOpen } = page.status
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
      <ClinicHud game={game} season={season} gameTime={gameTime} isOpen={isOpen} />

      <BottomActionBar activePanel={page.interaction.activePanel} onOpenPanel={page.openPanel} />

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
    </div>
  )
}
