import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import careerService from '../../services/career.service'

const emptyForm = { careerCode: '', careerName: '', careerDescription: '' }

function AddEditCareer() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [form, setForm] = useState(emptyForm)
  const [error, setError] = useState('')
  const isEdit = Boolean(id)

  useEffect(() => {
    if (!id) return
    careerService
      .get(id)
      .then((response) => {
        const { careerCode, careerName, careerDescription } = response.data
        setForm({ careerCode, careerName, careerDescription })
      })
      .catch((err) => setError(err.message))
  }, [id])

  const handleChange = (event) => {
    const { name, value } = event.target
    setForm({ ...form, [name]: value })
  }

  const handleSubmit = (event) => {
    event.preventDefault()
    setError('')
    const request = isEdit ? careerService.update(id, form) : careerService.create(form)
    request.then(() => navigate('/admin/careers')).catch((err) => setError(err.message))
  }

  return (
    <div>
      <h1>{isEdit ? 'Edit career' : 'New career'}</h1>
      {error && <p>{error}</p>}
      <form onSubmit={handleSubmit}>
        <label>
          Code
          <input name="careerCode" value={form.careerCode} onChange={handleChange} />
        </label>
        <label>
          Name
          <input name="careerName" value={form.careerName} onChange={handleChange} />
        </label>
        <label>
          Description
          <textarea name="careerDescription" value={form.careerDescription} onChange={handleChange} />
        </label>
        <button type="submit">Save</button>
        <Link to="/admin/careers">Cancel</Link>
      </form>
    </div>
  )
}

export default AddEditCareer
