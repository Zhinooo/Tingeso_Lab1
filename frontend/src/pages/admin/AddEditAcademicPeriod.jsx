import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import academicPeriodService from '../../services/academicPeriod.service'

const emptyForm = { academicYear: '', academicSemester: '' }

function AddEditAcademicPeriod() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [form, setForm] = useState(emptyForm)
  const [error, setError] = useState('')
  const isEdit = Boolean(id)

  useEffect(() => {
    if (!id) return
    academicPeriodService
      .get(id)
      .then((response) => {
        const { academicYear, academicSemester } = response.data
        setForm({ academicYear, academicSemester })
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
    const data = {
      academicYear: Number(form.academicYear),
      academicSemester: Number(form.academicSemester),
    }
    const request = isEdit ? academicPeriodService.update(id, data) : academicPeriodService.create(data)
    request.then(() => navigate('/admin/academic-periods')).catch((err) => setError(err.message))
  }

  return (
    <div>
      <h1>{isEdit ? 'Edit academic period' : 'New academic period'}</h1>
      {error && <p>{error}</p>}
      <form onSubmit={handleSubmit}>
        <label>
          Year
          <input type="number" name="academicYear" value={form.academicYear} onChange={handleChange} />
        </label>
        <label>
          Semester
          <select name="academicSemester" value={form.academicSemester} onChange={handleChange}>
            <option value="">Select</option>
            <option value="1">1</option>
            <option value="2">2</option>
          </select>
        </label>
        <button type="submit">Save</button>
        <Link to="/admin/academic-periods">Cancel</Link>
      </form>
    </div>
  )
}

export default AddEditAcademicPeriod
