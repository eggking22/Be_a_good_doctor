// 主场景患者立绘：公共目录资源可直接由浏览器按需加载。
export interface PatientPortrait {
  id: string
  name: string
  src: string
}

export const patientPortraits: PatientPortrait[] = [
  { id: 'elderly-man', name: '年长男患者', src: '/patients/patient-elderly-man.png' },
  { id: 'young-woman', name: '年轻女患者', src: '/patients/patient-young-woman.png' },
  { id: 'middle-aged-woman', name: '中年女患者', src: '/patients/patient-middle-aged-woman.png' },
  { id: 'young-man', name: '年轻男患者', src: '/patients/patient-young-man.png' },
  { id: 'elderly-woman', name: '年长女患者', src: '/patients/patient-elderly-woman.png' },
]
