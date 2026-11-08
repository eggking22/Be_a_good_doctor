import { useEffect, useRef, useState } from 'react'

import type { DialogueMessage } from '@/objects/consultation/DialogueMessage'
import type { ConsultationEndReason } from '@/objects/consultation/ConsultationEndReason'
import type { PrescriptionItem } from '@/objects/consultation/PrescriptionItem'
import type { PatientStage } from '@/pages/ClinicHomePage/objects/ClinicHomeViewState'
import type { PatientPortrait } from '@/pages/ClinicHomePage/objects/PatientPortrait'

interface PatientVisitOverlayProps {
  patient: PatientPortrait | null
  patientName: string | null
  stage: PatientStage
  messages: DialogueMessage[]
  isStarting: boolean
  isSendingText: boolean
  isEnding: boolean
  shoppingList: PrescriptionItem[]
  endReason: ConsultationEndReason | null
  error: string | null
  onSubmitText: (text: string) => void
  onFinishConsultation: () => Promise<void>
  onDismissEndedConsultation: () => void
}

export default function PatientVisitOverlay({ patient, patientName, stage, messages, isStarting, isSendingText, isEnding, shoppingList, endReason, error, onSubmitText, onFinishConsultation, onDismissEndedConsultation }: PatientVisitOverlayProps) {
  const [draft, setDraft] = useState('')
  const [arrivingItem, setArrivingItem] = useState<PrescriptionItem | null>(null)
  const knownShoppingItemIds = useRef<Set<string>>(new Set())

  const isConsulting = stage === 'consulting'
  const hasEnded = stage === 'ended'
  const validMessages = messages.filter(isDialogueMessage)
  const lastPatientMessage = [...validMessages].reverse().find((message) => message.speaker === 'patient')
  const lastPlayerMessage = [...validMessages].reverse().find((message) => message.speaker === 'player')

  useEffect(() => {
    if (!patient || stage === 'outside') {
      return
    }

    const newlyAdded = shoppingList.filter((item) => !knownShoppingItemIds.current.has(item.medicine.id))
    knownShoppingItemIds.current = new Set(shoppingList.map((item) => item.medicine.id))
    if (newlyAdded.length > 0) {
      setArrivingItem(newlyAdded.at(-1) ?? null)
    }
  }, [patient, stage, shoppingList])

  useEffect(() => {
    if (!arrivingItem) {
      return undefined
    }

    const timer = window.setTimeout(() => setArrivingItem(null), 2800)
    return () => window.clearTimeout(timer)
  }, [arrivingItem])

  if (!patient || stage === 'outside') {
    return null
  }

  function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!draft.trim() || isStarting || isSendingText) {
      return
    }
    onSubmitText(draft)
    setDraft('')
  }

  return (
    <>
      <section className="pointer-events-none absolute inset-0 z-20 flex items-end justify-center pb-[5vh]" aria-live="polite">
        <div className="relative flex h-[min(76vh,44rem)] w-[min(50vw,30rem)] items-end justify-center">
          <img
            src={patient.src}
            alt={`${patient.name}正在候诊`}
            draggable={false}
            className={`h-full max-w-full select-none object-contain object-bottom drop-shadow-[5px_7px_0_rgba(4,28,51,0.45)] ${stage === 'entering' ? 'patient-portrait-entering' : ''}`}
          />

          {isConsulting && (
            <div className="pointer-events-auto absolute left-1/2 top-[31%] z-10 w-[min(84vw,25rem)] -translate-x-1/2 translate-y-[5vh] text-[#113f63]">
              <div className="patient-dialogue-box px-3 py-2 text-[0.68rem] leading-5 sm:px-4">
                <p className="mb-1 text-[0.52rem] text-[#397ea8]">{patientName ?? '患者'}</p>
                <p>{isStarting ? '正在组织患者的开场陈述…' : lastPatientMessage?.content ?? '患者正在思考如何描述不适…'}</p>
              </div>
              <div className="controller-dialogue-box ml-3 mt-3 w-[calc(100%+1.25rem)] px-3 py-2 text-[0.58rem] leading-5 sm:px-4">
                <span className="text-[#775f2c]">主控</span>
                <span className="ml-2 text-[#927b43]">{lastPlayerMessage?.content ?? '请输入文字开始问诊。'}</span>
              </div>
              {isConsulting && (
                <form onSubmit={submit} className="pixel-card mt-3 flex gap-2 p-2">
                  <input
                    value={draft}
                    onChange={(event) => setDraft(event.target.value)}
                    disabled={isStarting || isSendingText || isEnding}
                    maxLength={500}
                    aria-label="输入对患者说的话"
                    placeholder={isSendingText ? '患者正在回应…' : '输入问诊内容…'}
                    className="min-w-0 flex-1 bg-transparent px-2 py-1 text-[0.62rem] outline-none placeholder:text-[#7ba3ba]"
                  />
                  <button type="submit" disabled={!draft.trim() || isStarting || isSendingText || isEnding} className="pixel-button px-3 py-1 text-[0.58rem] disabled:cursor-not-allowed disabled:opacity-50">
                    {isSendingText ? '思考中' : '发送'}
                  </button>
                </form>
              )}
              {hasEnded && <p className="mt-3 border-2 border-[#4b88aa] bg-[#dff3ff] px-3 py-2 text-[0.58rem] leading-4 text-[#275c7f]">{endMessage(endReason)}</p>}
              {error && <p role="alert" className="mt-2 border-2 border-[#a64e4e] bg-[#ffe2dc] px-3 py-2 text-[0.55rem] leading-4 text-[#8d3434]">{error}</p>}
            </div>
          )}
        </div>
      </section>

      {shoppingList.length > 0 && (
        <aside aria-label="本次购物清单" className="pointer-events-none absolute left-[4vw] top-1/2 z-30 w-[min(22vw,16rem)] min-w-[10rem] -translate-y-1/2 text-[#123f62]">
          <div className="shopping-list-panel p-3">
            <p className="text-[0.58rem] tracking-[0.08em] text-[#397ea8]">SHOPPING LIST</p>
            <h2 className="mt-1 text-[0.75rem] leading-5">本次购物清单</h2>
            <p className="mt-1 text-[0.52rem] text-[#6d7f8d]">未结算 · 已同步药房</p>
            <ul className="mt-3 space-y-2">
              {shoppingList.map((item) => (
                <li key={item.medicine.id} className="shopping-list-item flex items-center gap-2 px-2 py-1.5 text-[0.56rem] leading-4">
                  <img src={bottleAssetFor(item)} alt="" aria-hidden="true" className="shopping-list-bottle" />
                  <span className="min-w-0 flex-1 break-words">{item.medicine.name}</span>
                  <span className="whitespace-nowrap text-[#397ea8]">× {item.quantity}</span>
                </li>
              ))}
            </ul>
          </div>

          {arrivingItem && (
            <div key={arrivingItem.medicine.id} className="shopping-list-arrival" aria-live="polite">
              <span className="shopping-list-plus">+</span>
              <img src={bottleAssetFor(arrivingItem)} alt={`已加入购物清单：${arrivingItem.medicine.name}`} className="shopping-list-arrival-bottle" />
            </div>
          )}
        </aside>
      )}

      <section className="pointer-events-none absolute inset-x-0 bottom-24 z-[35] flex justify-center sm:bottom-28">
        <div className="flex flex-col items-center gap-2">
          <div className="border-2 border-[#143e61] bg-[#e9f8ff]/95 px-3 py-2 text-center text-[0.62rem] text-[#12476f] shadow-[3px_3px_0_#092a45]">
          {isConsulting ? '患者就诊中' : hasEnded ? '文字接诊已结束' : '患者正在进入'}
          </div>

          {isConsulting && !isStarting && (
            <button type="button" onClick={() => void onFinishConsultation()} disabled={isEnding || isSendingText} className="pixel-button pointer-events-auto w-max px-5 py-3 text-[0.72rem] disabled:cursor-not-allowed disabled:opacity-50">
              {isEnding ? '正在结束…' : '结束文字问诊'}
            </button>
          )}
          {hasEnded && <button type="button" onClick={onDismissEndedConsultation} className="pixel-button pointer-events-auto w-max px-5 py-3 text-[0.72rem]">返回接诊台</button>}
        </div>
      </section>
    </>
  )
}

function bottleAssetFor(item: PrescriptionItem): string {
  const id = item.medicine.id.toLowerCase()
  if (id.includes('vitamin') || id.includes('otc')) return '/medicines/medicine-bottle-green.png'
  if (id.includes('controlled') || id.includes('addictive')) return '/medicines/medicine-bottle-black.png'
  if (id.includes('high-alert') || id.includes('insulin') || id.includes('chemo') || id.includes('cardiac')) return '/medicines/medicine-bottle-yellow.png'
  return '/medicines/medicine-bottle-white.png'
}

function isDialogueMessage(value: unknown): value is DialogueMessage {
  return typeof value === 'object'
    && value !== null
    && 'speaker' in value
    && 'content' in value
    && typeof value.speaker === 'string'
    && typeof value.content === 'string'
}

function endMessage(reason: ConsultationEndReason | null): string {
  switch (reason) {
    case 'PlayerRequested': return '已按你的文字指令结束本次接诊，购物清单已保留。'
    case 'DoctorEnded': return '医生已结束本次文字问诊，购物清单已保留。'
    case 'PatientLeft': return '患者已道谢离场，购物清单已保留，等待后续结算。'
    case 'RoundLimitReached': return '已达到十个问答回合上限，文字问诊自动结束。'
    default: return '本次文字问诊已结束，购物清单已保留。'
  }
}
