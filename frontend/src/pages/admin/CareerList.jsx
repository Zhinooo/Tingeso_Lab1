import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import careerService from '../../services/career.service'

function CareerList() {
  const [careers, setCareers] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    careerService
      .getAll()
      .then((response) => setCareers(response.data))
      .catch((err) => setError(err.message))
  }, [])

  const handleToggleStatus = (career) => {
    const newStatus = career.careerStatus === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
    setError('')
    careerService
      .changeStatus(career.careerId, newStatus)
      .then((response) =>
        setCareers((current) =>
          current.map((item) => (item.careerId === career.careerId ? response.data : item)),
        ),
      )
      .catch((err) => setError(err.message))
  }

  const handleDelete = (career) => {
    if (!window.confirm(`Delete career ${career.careerName}?`)) return
    setError('')
    careerService
      .remove(career.careerId)
      .then(() => setCareers((current) => current.filter((item) => item.careerId !== career.careerId)))
      .catch((err) => setError(err.message))
  }

  return (
    <div>
      <h1>Careers</h1>
      {error && <p>{error}</p>}
      <Link to="/admin/careers/new">New career</Link>
      <table>
        <thead>
          <tr>
            <th>Code</th>
            <th>Name</th>
            <th>Description</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {careers.map((career) => (
            <tr key={career.careerId}>
              <td>{career.careerCode}</td>
              <td>{career.careerName}</td>
              <td>{career.careerDescription}</td>
              <td>{career.careerStatus}</td>
              <td>
                <Link to={`/admin/careers/${career.careerId}/edit`}>Edit</Link>
                <button onClick={() => handleToggleStatus(career)}>
                  {career.careerStatus === 'ACTIVE' ? 'Deactivate' : 'Activate'}
                </button>
                <button onClick={() => handleDelete(career)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default CareerList
