import type { APIMessage } from '@/system/api/APIMessage'
import { apiNameOf } from '@/system/api/apiNameOf'
import { apiRequest } from '@/system/api/apiRequest'

export function sendAPI<Response>(message: APIMessage<Response>): Promise<Response> {
  return apiRequest<Response>(
    `/api/messages/${message.serviceName}/requests/${apiNameOf(message)}`,
    message,
  )
}
