import { MAX_GRADE, MIN_GRADE, PASSING_GRADE } from '../constants/grades'

export const isValidGrade = (value) => value !== '' && Number(value) >= MIN_GRADE && Number(value) <= MAX_GRADE

export const resultOf = (value) => {
  if (!isValidGrade(value)) return null
  return Number(value) >= PASSING_GRADE ? 'APPROVED' : 'FAILED'
}

export const summarizePendingGrades = (enrollments) => {
  const pendingByCourse = {}
  enrollments
    .filter((enrollment) => enrollment.finalGrade == null)
    .forEach((enrollment) => {
      const entry = pendingByCourse[enrollment.courseCode] ?? {
        courseCode: enrollment.courseCode,
        courseName: enrollment.courseName,
        count: 0,
      }
      entry.count += 1
      pendingByCourse[enrollment.courseCode] = entry
    })
  const pending = Object.values(pendingByCourse).sort((a, b) => a.courseCode.localeCompare(b.courseCode))
  return {
    total: enrollments.length,
    pendingCount: pending.reduce((sum, entry) => sum + entry.count, 0),
    pending,
  }
}
