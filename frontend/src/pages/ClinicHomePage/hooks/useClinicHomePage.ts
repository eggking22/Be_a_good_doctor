import { useEffect, useRef, useState } from 'react'

import { GeneratePatientOpeningDialogueAPIMessage } from '@/apis/consultation/GeneratePatientOpeningDialogueAPIMessage'
import { EndConsultationAPIMessage } from '@/apis/consultation/EndConsultationAPIMessage'
import { StartConsultationAPIMessage } from '@/apis/consultation/StartConsultationAPIMessage'
import { SubmitPlayerTextAPIMessage } from '@/apis/consultation/SubmitPlayerTextAPIMessage'
import { GetGameStateAPIMessage } from '@/apis/game/GetGameStateAPIMessage'
import { StartGameAPIMessage } from '@/apis/game/StartGameAPIMessage'
import { GeneratePatientAPIMessage } from '@/apis/patient/GeneratePatientAPIMessage'
import type { DialogueMessage } from '@/objects/consultation/DialogueMessage'
import type { ClinicPanel, ClinicHomeInteractionState, ClinicHomeStatus } from '@/pages/ClinicHomePage/objects/ClinicHomeViewState'
import { patientPortraits } from '@/pages/ClinicHomePage/objects/PatientPortrait'
import { sendAPI } from '@/system/api/sendAPI'

const initialInteractionState: ClinicHomeInteractionState = {
  activePanel: null,
  patientStage: 'outside',
  patientPortrait: null,
  conversation: null,
  isStartingConsultation: false,
  isSendingText: false,
  isEndingConsultation: false,
  consultationError: null,
}

const gameStorageKey = 'ai-clinic-simulation.current-game-id'

export function useClinicHomePage() {
  const [status, setStatus] = useState<ClinicHomeStatus>({ status: 'loading' })
  const [interaction, setInteraction] = useState(initialInteractionState)
  const hasInitialized = useRef(false)

  useEffect(() => {
    if (hasInitialized.current) {
      return
    }
    hasInitialized.current = true

    void loadGame()
  }, [])

  useEffect(() => {
    if (interaction.patientStage !== 'entering') {
      return undefined
    }

    const timer = window.setTimeout(() => {
      setInteraction((current) => {
        if (current.patientStage !== 'entering') {
          return current
        }

        return { ...current, patientStage: 'consulting' }
      })
    }, 360)

    return () => window.clearTimeout(timer)
  }, [interaction.patientStage])

  async function loadGame() {
    setStatus({ status: 'loading' })
    try {
      const storedGameId = sessionStorage.getItem(gameStorageKey)
      const game = storedGameId
        ? await sendAPI(new GetGameStateAPIMessage(storedGameId)).catch(async () => {
            sessionStorage.removeItem(gameStorageKey)
            return sendAPI(new StartGameAPIMessage('医生'))
          })
        : await sendAPI(new StartGameAPIMessage('医生'))
      sessionStorage.setItem(gameStorageKey, game.id)
      setStatus({ status: 'ready', game, season: '春季', gameTime: '08:30', isOpen: true })
    } catch (error) {
      setStatus({ status: 'error', message: messageOf(error) })
    }
  }

  function openPanel(panel: ClinicPanel) {
    setInteraction((current) => ({ ...current, activePanel: panel }))
  }

  function closePanel() {
    setInteraction((current) => ({ ...current, activePanel: null }))
  }

  async function welcomePatient() {
    if (status.status !== 'ready' || interaction.patientStage !== 'outside' || interaction.isStartingConsultation) {
      return
    }

    const portrait = patientPortraits[Math.floor(Math.random() * patientPortraits.length)]
    setInteraction((current) => ({
      ...current,
      activePanel: null,
      patientStage: 'entering',
      patientPortrait: portrait,
      consultationError: null,
      isStartingConsultation: true,
    }))

    try {
      const patient = await sendAPI(new GeneratePatientAPIMessage(status.game.id))
      const started = await sendAPI(new StartConsultationAPIMessage(status.game.id, patient.id))
      const opening = await sendAPI(new GeneratePatientOpeningDialogueAPIMessage(started.consultationId))
      setInteraction((current) => ({
        ...current,
        conversation: {
          consultationId: started.consultationId,
          patient: started.patient,
          messages: [opening.patientMessage],
          latestAnalysis: null,
          shoppingList: [],
          endReason: null,
        },
        isStartingConsultation: false,
      }))
    } catch (error) {
      setInteraction((current) => ({
        ...current,
        activePanel: 'consultation',
        patientStage: 'outside',
        patientPortrait: null,
        conversation: null,
        isStartingConsultation: false,
        consultationError: messageOf(error),
      }))
    }
  }

  async function submitPlayerText(text: string) {
    const normalizedText = text.trim()
    const conversation = interaction.conversation
    if (!normalizedText || !conversation || interaction.isSendingText || interaction.isEndingConsultation || interaction.patientStage !== 'consulting') {
      return
    }

    setInteraction((current) => ({ ...current, isSendingText: true, consultationError: null }))
    try {
      const reply = await sendAPI(new SubmitPlayerTextAPIMessage(conversation.consultationId, normalizedText))
      setInteraction((current) => {
        if (!current.conversation || current.conversation.consultationId !== conversation.consultationId) {
          return current
        }
        return {
          ...current,
          isSendingText: false,
          conversation: {
            ...current.conversation,
            messages: [...current.conversation.messages, ...validDialogueMessages(reply.playerMessage, reply.patientMessage)],
            latestAnalysis: reply.analysis,
            shoppingList: reply.shoppingList,
            endReason: reply.endReason,
          },
          patientStage: reply.endReason ? 'ended' : current.patientStage,
        }
      })
    } catch (error) {
      setInteraction((current) => ({ ...current, isSendingText: false, consultationError: messageOf(error) }))
    }
  }

  async function finishConsultation() {
    const conversation = interaction.conversation
    if (!conversation || interaction.isEndingConsultation || interaction.patientStage !== 'consulting') {
      return
    }

    setInteraction((current) => ({ ...current, isEndingConsultation: true, consultationError: null }))
    try {
      const ended = await sendAPI(new EndConsultationAPIMessage(conversation.consultationId))
      setInteraction((current) => {
        if (!current.conversation || current.conversation.consultationId !== conversation.consultationId) {
          return current
        }
        return {
          ...current,
          patientStage: 'ended',
          isEndingConsultation: false,
          conversation: {
            ...current.conversation,
            messages: [...current.conversation.messages, ...validDialogueMessages(ended.playerMessage, ended.patientMessage)],
            latestAnalysis: isPlayerTextAnalysis(ended.analysis) ? ended.analysis : current.conversation.latestAnalysis,
            shoppingList: Array.isArray(ended.shoppingList) ? ended.shoppingList : current.conversation.shoppingList,
            endReason: ended.endReason ?? 'DoctorEnded',
          },
        }
      })
    } catch (error) {
      setInteraction((current) => ({ ...current, isEndingConsultation: false, consultationError: messageOf(error) }))
    }
  }

  function dismissEndedConsultation() {
    setInteraction((current) => ({
      ...current,
      activePanel: null,
      patientStage: 'outside',
      patientPortrait: null,
      conversation: null,
      isStartingConsultation: false,
      isSendingText: false,
      isEndingConsultation: false,
      consultationError: null,
    }))
  }

  return {
    status,
    interaction,
    openPanel,
    closePanel,
    welcomePatient,
    submitPlayerText,
    finishConsultation,
    dismissEndedConsultation,
    retryGame: loadGame,
  }
}

function messageOf(error: unknown): string {
  return error instanceof Error ? error.message : '请求失败，请稍后重试。'
}

function validDialogueMessages(...values: unknown[]): DialogueMessage[] {
  return values.filter(isDialogueMessage)
}

function isDialogueMessage(value: unknown): value is DialogueMessage {
  return typeof value === 'object'
    && value !== null
    && 'speaker' in value
    && 'content' in value
    && 'createdAt' in value
    && typeof value.speaker === 'string'
    && typeof value.content === 'string'
    && typeof value.createdAt === 'string'
}

function isPlayerTextAnalysis(value: unknown): value is NonNullable<ClinicHomeInteractionState['conversation']>['latestAnalysis'] {
  return typeof value === 'object'
    && value !== null
    && 'attitude' in value
    && 'intent' in value
    && 'mentionedMedicineIds' in value
    && 'confidence' in value
    && typeof value.attitude === 'string'
    && typeof value.intent === 'string'
    && Array.isArray(value.mentionedMedicineIds)
    && typeof value.confidence === 'number'
}
