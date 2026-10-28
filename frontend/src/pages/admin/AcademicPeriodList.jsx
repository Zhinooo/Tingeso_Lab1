import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import academicPeriodService from '../../services/academicPeriod.service'

function AcademicPeriodList() {
  const [periods, setPeriods] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    academicPeriodService
      .getAll()
      .then((response) => setPeriods(response.data))
      .catch((err) => setError(err.message))
  }, [])

  const handleClose = (period) => {
    const name = `${period.academicYear}-${period.academicSemester}`
    if (!window.confirm(`Close academic period ${name}? This cannot be undone.`)) return
    setError('')
    academicPeriodService
      .close(period.academicPeriodId)
      .then((response) =>
        setPeriods((current) =>
          current.map((item) => (item.academicPeriodId === period.academicPeriodId ? response.data : item)),
        ),
      )
      .catch((err) => setError(err.message))
  }

  const handleDelete = (period) => {
    if (!window.confirm(`Delete academic period ${period.academicYear}-${period.academicSemester}?`)) return
    setError('')
    academicPeriodService
      .remove(period.academicPeriodId)
      .then(() =>
        setPeriods((current) => current.filter((item) => item.academicPeriodId !== period.academicPeriodId)),
      )
      .catch((err) => setError(err.message))
  }

  return (
    <div>
      <h1>Academic periods</h1>
      {error && <p>{error}</p>}
      <Link to="/admin/academic-periods/new">New academic period</Link>
      <table>
        <thead>
          <tr>
            <th>Period</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {periods.map((period) => (
            <tr key={period.academicPeriodId}>
              <td>{`${period.academicYear}-${period.academicSemester}`}</td>
              <td>{period.academicPeriodStatus}</td>
              <td>
                <Link to={`/admin/sections?academicPeriodId=${period.academicPeriodId}`}>Sections</Link>
                <Link to={`/admin/academic-periods/${period.academicPeriodId}/edit`}>Edit</Link>
                {period.academicPeriodStatus === 'OPEN' && (
                  <button onClick={() => handleClose(period)}>Close period</button>
                )}
                <button onClick={() => handleDelete(period)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default AcademicPeriodList
