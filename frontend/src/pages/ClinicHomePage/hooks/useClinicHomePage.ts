import { useEffect, useState } from 'react'

import type { ClinicPanel, ClinicHomeInteractionState, ClinicHomeStatus } from '@/pages/ClinicHomePage/objects/ClinicHomeViewState'

const initialInteractionState: ClinicHomeInteractionState = {
  activePanel: null,
  patientStage: 'outside',
}

export function useClinicHomePage(status: ClinicHomeStatus) {
  const [interaction, setInteraction] = useState(initialInteractionState)

  useEffect(() => {
    if (interaction.patientStage === 'outside' || interaction.patientStage === 'consulting') {
      return undefined
    }

    const stage = interaction.patientStage
    const transitionDuration = stage === 'entering' ? 900 : 700
    const timer = window.setTimeout(() => {
      setInteraction((current) => {
        if (current.patientStage !== stage) {
          return current
        }

        if (stage === 'entering') {
          return { ...current, patientStage: 'consulting' }
        }

        return { ...current, patientStage: 'outside', activePanel: null }
      })
    }, transitionDuration)

    return () => window.clearTimeout(timer)
  }, [interaction.patientStage])

  function openPanel(panel: ClinicPanel) {
    setInteraction((current) => ({ ...current, activePanel: panel }))
  }

  function closePanel() {
    setInteraction((current) => ({ ...current, activePanel: null }))
  }

  function welcomePatient() {
    setInteraction((current) => {
      if (current.patientStage !== 'outside') {
        return current
      }

      return {
        ...current,
        activePanel: 'consultation',
        patientStage: 'entering',
      }
    })
  }

  function finishConsultation() {
    setInteraction((current) => ({
      ...current,
      patientStage: 'leaving',
    }))
  }

  return {
    status,
    interaction,
    openPanel,
    closePanel,
    welcomePatient,
    finishConsultation,
  }
}
