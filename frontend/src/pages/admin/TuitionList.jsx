import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import academicPeriodService from '../../services/academicPeriod.service'
import tuitionService from '../../services/tuition.service'

function TuitionList() {
  const [academicPeriodId, setAcademicPeriodId] = useState('')
  const [periods, setPeriods] = useState([])
  const [tuitions, setTuitions] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    academicPeriodService
      .getAll()
      .then((response) => setPeriods(response.data))
      .catch((err) => setError(err.message))
  }, [])

  useEffect(() => {
    const request = academicPeriodId
      ? tuitionService.getByAcademicPeriod(academicPeriodId)
      : tuitionService.getAll()
    request.then((response) => setTuitions(response.data)).catch((err) => setError(err.message))
  }, [academicPeriodId])

  const handleDelete = (tuition) => {
    if (!window.confirm(`Delete tuition of ${tuition.studentRun}?`)) return
    setError('')
    tuitionService
      .remove(tuition.tuitionId)
      .then(() => setTuitions((current) => current.filter((item) => item.tuitionId !== tuition.tuitionId)))
      .catch((err) => setError(err.message))
  }

  return (
    <div>
      <h1>Tuitions</h1>
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
      <Link to="/admin/tuitions/new">New tuition</Link>
      <table>
        <thead>
          <tr>
            <th>Student RUN</th>
            <th>Period</th>
            <th>Date</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {tuitions.map((tuition) => (
            <tr key={tuition.tuitionId}>
              <td>{tuition.studentRun}</td>
              <td>{`${tuition.academicYear}-${tuition.academicSemester}`}</td>
              <td>{tuition.tuitionDate}</td>
              <td>
                <button onClick={() => handleDelete(tuition)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default TuitionList
