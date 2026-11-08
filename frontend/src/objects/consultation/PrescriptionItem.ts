import type { Medicine } from '@/objects/pharmacy/Medicine'

// 与后端 PrescriptionItem 对齐：药品识别后写入本次接诊购物清单。
export interface PrescriptionItem {
  medicine: Medicine
  quantity: number
}
