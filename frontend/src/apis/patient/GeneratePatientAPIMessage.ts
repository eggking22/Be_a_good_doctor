import { APIMessage } from '@/system/api/APIMessage'

import type { Patient } from '@/objects/patient/Patient'

// 与后端 GeneratePatientAPIMessage 一一对应：每次请求只生成一名患者。
export class GeneratePatientAPIMessage extends APIMessage<Patient> {
  readonly gameId: string

  constructor(gameId: string) {
    super()
    this.gameId = gameId
  }

  get serviceName(): string {
    return 'patient'
  }
}
