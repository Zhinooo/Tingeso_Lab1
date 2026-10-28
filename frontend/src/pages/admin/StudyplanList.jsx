import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import studyplanService from '../../services/studyplan.service'

function StudyplanList() {
  const [studyplans, setStudyplans] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    studyplanService
      .getAll()
      .then((response) => setStudyplans(response.data))
      .catch((err) => setError(err.message))
  }, [])

  const handleToggleStatus = (studyplan) => {
    const newStatus = studyplan.studyplanStatus === 'CURRENT' ? 'NOT_CURRENT' : 'CURRENT'
    setError('')
    studyplanService
      .changeStatus(studyplan.studyplanId, newStatus)
      .then(() => studyplanService.getAll())
      .then((response) => setStudyplans(response.data))
      .catch((err) => setError(err.message))
  }

  const handleDelete = (studyplan) => {
    if (!window.confirm(`Delete study plan ${studyplan.studyplanIdentifier}?`)) return
    setError('')
    studyplanService
      .remove(studyplan.studyplanId)
      .then(() =>
        setStudyplans((current) => current.filter((item) => item.studyplanId !== studyplan.studyplanId)),
      )
      .catch((err) => setError(err.message))
  }

  return (
    <div>
      <h1>Study plans</h1>
      {error && <p>{error}</p>}
      <Link to="/admin/studyplans/new">New study plan</Link>
      <table>
        <thead>
          <tr>
            <th>Identifier</th>
            <th>Career</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {studyplans.map((studyplan) => (
            <tr key={studyplan.studyplanId}>
              <td>{studyplan.studyplanIdentifier}</td>
              <td>{studyplan.careerCode}</td>
              <td>{studyplan.studyplanStatus}</td>
              <td>
                <Link to={`/admin/courses?studyplanId=${studyplan.studyplanId}`}>Courses</Link>
                <Link to={`/admin/studyplans/${studyplan.studyplanId}/edit`}>Edit</Link>
                <button onClick={() => handleToggleStatus(studyplan)}>
                  {studyplan.studyplanStatus === 'CURRENT' ? 'Set not current' : 'Set current'}
                </button>
                <button onClick={() => handleDelete(studyplan)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default StudyplanList
