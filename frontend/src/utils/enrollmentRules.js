import { MAX_COURSES, MIN_COURSES } from '../constants/enrollments'

const blockOwners = (current, selected) => {
  const owners = {}
  current.forEach((item) => item.blocks.forEach((block) => (owners[block] = { ...item, enrolled: true })))
  const conflicts = []
  selected.forEach((course) =>
    course.scheduleBlocks.forEach((block) => {
      const owner = owners[block]
      if (owner) {
        conflicts.push({ block, first: owner, second: course })
      }
      owners[block] = course
    }),
  )
  return { owners, conflicts }
}

export const findConflictingCourses = (course, current, selected) => {
  const { owners } = blockOwners(current, selected.filter((item) => item.courseId !== course.courseId))
  return [...new Set(course.scheduleBlocks.filter((block) => owners[block]).map((block) => owners[block].courseCode))]
}

const countCheck = (total, availableCount) => {
  const minimum = Math.min(MIN_COURSES, availableCount)
  return {
    ok: total >= minimum && total <= MAX_COURSES,
    text: `Ramos en el período: ${total} (mínimo ${minimum}, máximo ${MAX_COURSES})`,
  }
}

const semesterOrderCheck = (selected, offer) => {
  const selectedIds = selected.map((course) => course.courseId)
  for (const course of selected) {
    const pending = offer.find((other) => other.semester < course.semester && !selectedIds.includes(other.courseId))
    if (pending) {
      return {
        ok: false,
        text: `Para tomar ${course.courseCode} (semestre ${course.semester}) también debes incluir ${pending.courseCode} (semestre ${pending.semester})`,
      }
    }
  }
  return { ok: true, text: 'Sin ramos pendientes de semestres anteriores' }
}

const conflictText = ({ block, first, second }) => {
  if (first.enrolled) {
    return `Tope en ${block}: ${second.courseCode} choca con ${first.courseCode}, que ya tienes inscrito`
  }
  if (first.semester !== second.semester) {
    const [lower, higher] = first.semester < second.semester ? [first, second] : [second, first]
    return `Tope en ${block}: ${first.courseCode} y ${second.courseCode}. Quédate con ${lower.courseCode} (semestre ${lower.semester}) y quita ${higher.courseCode}`
  }
  return `Tope en ${block}: ${first.courseCode} y ${second.courseCode}`
}

const scheduleCheck = (current, selected) => {
  const { conflicts } = blockOwners(current, selected)
  if (conflicts.length > 0) {
    return { ok: false, text: conflictText(conflicts[0]) }
  }
  return { ok: true, text: 'Sin topes de horario' }
}

export const buildEnrollmentChecks = ({ offer, selected, current }) => [
  countCheck(current.length + selected.length, offer.length + current.length),
  semesterOrderCheck(selected, offer),
  scheduleCheck(current, selected),
]
