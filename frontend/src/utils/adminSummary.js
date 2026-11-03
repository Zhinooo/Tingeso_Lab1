import { STUDENT_STATUSES } from '../constants/students'
import { summarizePendingGrades } from './grades'
import { comparePeriodsDesc } from './periods'

export const findCurrentPeriod = (periods) =>
  [...periods].sort(comparePeriodsDesc).find((period) => period.academicPeriodStatus === 'OPEN') ?? null

export const buildAdminSummary = ({ periods, students, sections, enrollments }) => ({
  currentPeriod: findCurrentPeriod(periods),
  openPeriodCount: periods.filter((period) => period.academicPeriodStatus === 'OPEN').length,
  studentCount: students.length,
  studentsByStatus: STUDENT_STATUSES.map((status) => ({
    status,
    count: students.filter((student) => student.studentStatus === status).length,
  })).filter((entry) => entry.count > 0),
  sectionCount: sections.length,
  enrollmentCount: enrollments.length,
  pendingGradeCount: summarizePendingGrades(enrollments).pendingCount,
})
