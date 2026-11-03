export const buildCourseStatuses = (history, enrollments) => {
  const statuses = {}
  const historyIds = new Set(history.map((enrollment) => enrollment.enrollmentId))

  history
    .filter((enrollment) => enrollment.result === 'FAILED')
    .forEach((enrollment) => (statuses[enrollment.courseId] = 'FAILED'))
  enrollments
    .filter((enrollment) => !historyIds.has(enrollment.enrollmentId))
    .forEach((enrollment) => (statuses[enrollment.courseId] = 'IN_PROGRESS'))
  history
    .filter((enrollment) => enrollment.result === 'APPROVED')
    .forEach((enrollment) => (statuses[enrollment.courseId] = 'APPROVED'))

  return statuses
}

export const summarizeProgress = (courses, statuses) => {
  const approved = courses.filter((course) => statuses[course.courseId] === 'APPROVED')
  const sumSct = (list) => list.reduce((sum, course) => sum + course.sct, 0)

  return {
    approvedCount: approved.length,
    totalCount: courses.length,
    approvedSct: sumSct(approved),
    totalSct: sumSct(courses),
    percent: courses.length ? Math.round((approved.length / courses.length) * 100) : 0,
  }
}
