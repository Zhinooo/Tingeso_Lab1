import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import careerService from '../../services/career.service'
import studyplanService from '../../services/studyplan.service'

const emptyForm = { studyplanIdentifier: '', careerCode: '', current: true }

function AddEditStudyplan() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [form, setForm] = useState(emptyForm)
  const [careers, setCareers] = useState([])
  const [error, setError] = useState('')
  const isEdit = Boolean(id)

  useEffect(() => {
    careerService
      .getAll()
      .then((response) => setCareers(response.data))
      .catch((err) => setError(err.message))
  }, [])

  useEffect(() => {
    if (!id) return
    studyplanService
      .get(id)
      .then((response) => {
        const { studyplanIdentifier, careerCode, studyplanStatus } = response.data
        setForm({ studyplanIdentifier, careerCode, current: studyplanStatus === 'CURRENT' })
      })
      .catch((err) => setError(err.message))
  }, [id])

  const handleChange = (event) => {
    const { name, value, type, checked } = event.target
    setForm({ ...form, [name]: type === 'checkbox' ? checked : value })
  }

  const handleSubmit = (event) => {
    event.preventDefault()
    setError('')
    const request = isEdit ? studyplanService.update(id, form) : studyplanService.create(form)
    request.then(() => navigate('/admin/studyplans')).catch((err) => setError(err.message))
  }

  return (
    <div>
      <h1>{isEdit ? 'Edit study plan' : 'New study plan'}</h1>
      {error && <p>{error}</p>}
      <form onSubmit={handleSubmit}>
        <label>
          Identifier
          <input name="studyplanIdentifier" value={form.studyplanIdentifier} onChange={handleChange} />
        </label>
        <label>
          Career
          <select name="careerCode" value={form.careerCode} onChange={handleChange} disabled={isEdit}>
            <option value="">Select a career</option>
            {careers.map((career) => (
              <option key={career.careerId} value={career.careerCode}>
                {career.careerCode} - {career.careerName}
              </option>
            ))}
          </select>
        </label>
        {!isEdit && (
          <label>
            <input type="checkbox" name="current" checked={form.current} onChange={handleChange} />
            Current plan
          </label>
        )}
        <button type="submit">Save</button>
        <Link to="/admin/studyplans">Cancel</Link>
      </form>
    </div>
  )
}

export default AddEditStudyplan
