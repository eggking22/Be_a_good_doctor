import type { APIMessage } from '@/system/api/APIMessage'

// 与后端 RegisteredAPIMessage 保持一致：移除 Message 后转小写。
export function apiNameOf(message: APIMessage<unknown>): string {
  return message.constructor.name.replace(/Message$/, '').toLowerCase()
}
