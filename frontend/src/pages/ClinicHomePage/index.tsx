import type { ClinicHomeStatus } from '@/pages/ClinicHomePage/objects/ClinicHomeViewState'
import BottomActionBar from '@/pages/ClinicHomePage/components/BottomActionBar'
import ClinicHud from '@/pages/ClinicHomePage/components/ClinicHud'
import ClinicRoomScene from '@/pages/ClinicHomePage/components/ClinicRoomScene'
import ClinicSidePanel from '@/pages/ClinicHomePage/components/ClinicSidePanel'
import { useClinicHomePage } from '@/pages/ClinicHomePage/hooks/useClinicHomePage'

const initialStatus: ClinicHomeStatus = {
  status: 'ready',
  game: {
    id: 'game-preview',
    playerName: '医生',
    currentDay: 1,
    finance: {
      cash: 10000,
      cashFlow: { income: 860, expense: 240 },
      dailyStatement: null,
    },
    reputation: { score: 60 },
    clinic: { facilities: [], employees: [] },
    inventory: { items: [] },
    bankrupt: false,
    createdAt: '2026-11-06T08:00:00+08:00',
    updatedAt: '2026-11-06T08:00:00+08:00',
  },
  season: '春季',
  gameTime: '08:30',
  isOpen: true,
}

export default function ClinicHomePage() {
  const page = useClinicHomePage(initialStatus)

  if (page.status.status !== 'ready') {
    return <div className="flex min-h-screen items-center justify-center bg-[#f4e6d1] p-6 text-center font-bold text-[#71564c]">{page.status.message}</div>
  }

  const { game, season, gameTime, isOpen } = page.status
  const isPatientPresent = page.interaction.patientStage !== 'outside'

  return (
    <div className="relative h-screen min-h-[42rem] overflow-hidden bg-[#657665] font-sans selection:bg-[#dcc080] selection:text-[#294238]">
      <ClinicRoomScene />
      <ClinicHud game={game} season={season} gameTime={gameTime} isOpen={isOpen} />

      <BottomActionBar activePanel={page.interaction.activePanel} onOpenPanel={page.openPanel} />

      {page.interaction.activePanel && (
        <ClinicSidePanel
          panel={page.interaction.activePanel}
          isPatientPresent={isPatientPresent}
          onClose={page.closePanel}
          onWelcomePatient={page.welcomePatient}
          onFinishConsultation={page.finishConsultation}
        />
      )}
    </div>
  )
}
