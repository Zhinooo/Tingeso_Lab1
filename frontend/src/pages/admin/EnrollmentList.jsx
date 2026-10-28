import { useEffect, useState } from 'react'
import academicPeriodService from '../../services/academicPeriod.service'
import enrollmentService from '../../services/enrollment.service'

function EnrollmentList() {
  const [academicPeriodId, setAcademicPeriodId] = useState('')
  const [periods, setPeriods] = useState([])
  const [enrollments, setEnrollments] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    academicPeriodService
      .getAll()
      .then((response) => setPeriods(response.data))
      .catch((err) => setError(err.message))
  }, [])

  useEffect(() => {
    const request = academicPeriodId
      ? enrollmentService.getByAcademicPeriod(academicPeriodId)
      : enrollmentService.getAll()
    request.then((response) => setEnrollments(response.data)).catch((err) => setError(err.message))
  }, [academicPeriodId])

  return (
    <div>
      <h1>Enrollments</h1>
      {error && <p>{error}</p>}
      <label>
        Academic period
        <select value={academicPeriodId} onChange={(event) => setAcademicPeriodId(event.target.value)}>
          <option value="">All</option>
          {periods.map((period) => (
            <option key={period.academicPeriodId} value={period.academicPeriodId}>
              {`${period.academicYear}-${period.academicSemester}`}
            </option>
          ))}
        </select>
      </label>
      <table>
        <thead>
          <tr>
            <th>Student RUN</th>
            <th>Period</th>
            <th>Code</th>
            <th>Course</th>
            <th>Date</th>
            <th>Final grade</th>
            <th>Result</th>
          </tr>
        </thead>
        <tbody>
          {enrollments.map((enrollment) => (
            <tr key={enrollment.enrollmentId}>
              <td>{enrollment.studentRun}</td>
              <td>{`${enrollment.academicYear}-${enrollment.academicSemester}`}</td>
              <td>{enrollment.courseCode}</td>
              <td>{enrollment.courseName}</td>
              <td>{enrollment.enrollmentDate}</td>
              <td>{enrollment.finalGrade}</td>
              <td>{enrollment.result}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default EnrollmentList
