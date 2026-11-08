import type { MedicalCatalog } from '@/objects/pharmacy/MedicalCatalog'
import { APIMessage } from '@/system/api/APIMessage'

// 与后端 GetMedicalCatalogAPIMessage 一一对应：读取已导入的医疗目录。
export class GetMedicalCatalogAPIMessage extends APIMessage<MedicalCatalog> {
  get serviceName(): string {
    return 'pharmacy'
  }
}
