export const formatPeriod = (item) => `${item.academicYear}-${item.academicSemester}`

export const comparePeriodsDesc = (a, b) =>
  b.academicYear - a.academicYear || b.academicSemester - a.academicSemester
