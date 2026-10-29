import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import teacherService from '../../services/teacher.service'

const emptyForm = {
  teacherRun: '',
  teacherFullname: '',
  teacherEmail: '',
  teacherProtitle: '',
  teacherDegree: '',
  password: '',
}

function AddEditTeacher() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [form, setForm] = useState(emptyForm)
  const [error, setError] = useState('')
  const isEdit = Boolean(id)

  useEffect(() => {
    if (!id) return
    teacherService
      .get(id)
      .then((response) => {
        const teacher = response.data
        setForm({
          teacherRun: teacher.teacherRun,
          teacherFullname: teacher.teacherFullname,
          teacherEmail: teacher.teacherEmail,
          teacherProtitle: teacher.teacherProtitle,
          teacherDegree: teacher.teacherDegree,
          password: '',
        })
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
    const request = isEdit
      ? teacherService.update(id, {
          teacherFullname: form.teacherFullname,
          teacherEmail: form.teacherEmail,
          teacherProtitle: form.teacherProtitle,
          teacherDegree: form.teacherDegree,
        })
      : teacherService.create(form)
    request.then(() => navigate('/admin/teachers')).catch((err) => setError(err.message))
  }

  return (
    <div>
      <h1>{isEdit ? 'Edit teacher' : 'New teacher'}</h1>
      {error && <p>{error}</p>}
      <form onSubmit={handleSubmit}>
        <label>
          RUN
          <input name="teacherRun" value={form.teacherRun} onChange={handleChange} disabled={isEdit} />
          <small>Enter the RUT as 12345678-k</small>
        </label>
        <label>
          Full name
          <input name="teacherFullname" value={form.teacherFullname} onChange={handleChange} />
        </label>
        <label>
          Email
          <input type="email" name="teacherEmail" value={form.teacherEmail} onChange={handleChange} />
        </label>
        <label>
          Professional title
          <input name="teacherProtitle" value={form.teacherProtitle} onChange={handleChange} />
        </label>
        <label>
          Academic degree
          <input name="teacherDegree" value={form.teacherDegree} onChange={handleChange} />
        </label>
        {!isEdit && (
          <label>
            Temporary password
            <input type="password" name="password" value={form.password} onChange={handleChange} />
          </label>
        )}
        <button type="submit">Save</button>
        <Link to="/admin/teachers">Cancel</Link>
      </form>
    </div>
  )
}

export default AddEditTeacher
