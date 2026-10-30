import { MAX_GRADE, MIN_GRADE, PASSING_GRADE } from '../constants/grades'

export const isValidGrade = (value) => value !== '' && Number(value) >= MIN_GRADE && Number(value) <= MAX_GRADE

export const resultOf = (value) => {
  if (!isValidGrade(value)) return null
  return Number(value) >= PASSING_GRADE ? 'APPROVED' : 'FAILED'
}
