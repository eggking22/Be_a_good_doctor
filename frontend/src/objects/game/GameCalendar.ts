const seasonNames = ['春季', '夏季', '秋季', '冬季'] as const
const weekdayNames = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] as const

const daysPerWeek = 7
const weeksPerSeason = 4
const daysPerSeason = daysPerWeek * weeksPerSeason
const weeksPerYear = 16
const daysPerYear = daysPerWeek * weeksPerYear

export function calendarForDay(currentDay: number) {
  const normalizedDay = Math.max(1, Math.floor(currentDay)) - 1
  const year = Math.floor(normalizedDay / daysPerYear) + 1
  const dayOfYear = normalizedDay % daysPerYear
  const seasonIndex = Math.floor(dayOfYear / daysPerSeason)
  const dayOfSeason = dayOfYear % daysPerSeason

  return {
    year,
    season: seasonNames[seasonIndex],
    weekOfSeason: Math.floor(dayOfSeason / daysPerWeek) + 1,
    weekday: weekdayNames[dayOfSeason % daysPerWeek],
  }
}

export function formatGameTime(totalMinutes: number): string {
  const normalizedMinutes = Math.max(0, Math.min(24 * 60, Math.floor(totalMinutes)))
  const hour = Math.floor(normalizedMinutes / 60).toString().padStart(2, '0')
  const minute = (normalizedMinutes % 60).toString().padStart(2, '0')
  return `${hour}:${minute}`
}

export function hasBusinessStarted(totalMinutes: number): boolean {
  return totalMinutes >= 9 * 60
}

export function hasBusinessEnded(totalMinutes: number): boolean {
  return totalMinutes >= 17 * 60
}
