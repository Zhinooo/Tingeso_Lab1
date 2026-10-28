import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import academicPeriodService from '../../services/academicPeriod.service'
import studentService from '../../services/student.service'
import tuitionService from '../../services/tuition.service'

const emptyForm = { studentId: '', academicPeriodId: '' }

function AddTuition() {
  const navigate = useNavigate()
  const [form, setForm] = useState(emptyForm)
  const [students, setStudents] = useState([])
  const [periods, setPeriods] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([studentService.getAll(), academicPeriodService.getAll()])
      .then(([studentsResponse, periodsResponse]) => {
        setStudents(studentsResponse.data)
        setPeriods(periodsResponse.data.filter((period) => period.academicPeriodStatus === 'OPEN'))
      })
      .catch((err) => setError(err.message))
  }, [])

  const handleChange = (event) => {
    const { name, value } = event.target
    setForm({ ...form, [name]: value })
  }

  const handleSubmit = (event) => {
    event.preventDefault()
    setError('')
    tuitionService
      .create({ studentId: Number(form.studentId), academicPeriodId: Number(form.academicPeriodId) })
      .then(() => navigate('/admin/tuitions'))
      .catch((err) => setError(err.message))
  }

  return (
    <div>
      <h1>New tuition</h1>
      {error && <p>{error}</p>}
      <form onSubmit={handleSubmit}>
        <label>
          Student
          <select name="studentId" value={form.studentId} onChange={handleChange}>
            <option value="">Select a student</option>
            {students.map((student) => (
              <option key={student.studentId} value={student.studentId}>
                {student.studentRun} - {student.studentFullname}
              </option>
            ))}
          </select>
        </label>
        <label>
          Academic period
          <select name="academicPeriodId" value={form.academicPeriodId} onChange={handleChange}>
            <option value="">Select a period</option>
            {periods.map((period) => (
              <option key={period.academicPeriodId} value={period.academicPeriodId}>
                {`${period.academicYear}-${period.academicSemester}`}
              </option>
            ))}
          </select>
        </label>
        <button type="submit">Save</button>
        <Link to="/admin/tuitions">Cancel</Link>
      </form>
    </div>
  )
}

export default AddTuition
