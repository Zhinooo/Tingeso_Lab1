import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import careerService from '../../services/career.service'
import studentService from '../../services/student.service'
import studyplanService from '../../services/studyplan.service'

const emptyForm = {
  studentRun: '',
  studentFullname: '',
  studentEmail: '',
  careerCode: '',
  password: '',
}

const emptyCareerForm = { careerCode: '', studyplanIdentifier: '' }

function AddEditStudent() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [form, setForm] = useState(emptyForm)
  const [careerForm, setCareerForm] = useState(emptyCareerForm)
  const [careers, setCareers] = useState([])
  const [studyplans, setStudyplans] = useState([])
  const [error, setError] = useState('')
  const isEdit = Boolean(id)

  useEffect(() => {
    Promise.all([careerService.getAll(), studyplanService.getAll()])
      .then(([careersResponse, studyplansResponse]) => {
        setCareers(careersResponse.data)
        setStudyplans(studyplansResponse.data)
      })
      .catch((err) => setError(err.message))
  }, [])

  useEffect(() => {
    if (!id) return
    studentService
      .get(id)
      .then((response) => {
        const student = response.data
        setForm({
          studentRun: student.studentRun,
          studentFullname: student.studentFullname,
          studentEmail: student.studentEmail,
          careerCode: student.careerCode,
          password: '',
        })
        setCareerForm({ careerCode: student.careerCode, studyplanIdentifier: student.studyplanIdentifier })
      })
      .catch((err) => setError(err.message))
  }, [id])

  const activeCareers = careers.filter((career) => career.careerStatus === 'ACTIVE')
  const careerStudyplans = studyplans.filter((studyplan) => studyplan.careerCode === careerForm.careerCode)

  const handleChange = (event) => {
    const { name, value } = event.target
    setForm({ ...form, [name]: value })
  }

  const handleCareerChange = (event) => {
    const { name, value } = event.target
    if (name === 'careerCode') {
      setCareerForm({ careerCode: value, studyplanIdentifier: '' })
    } else {
      setCareerForm({ ...careerForm, [name]: value })
    }
  }

  const handleSubmit = (event) => {
    event.preventDefault()
    setError('')
    const request = isEdit
      ? studentService.update(id, { studentFullname: form.studentFullname, studentEmail: form.studentEmail })
      : studentService.create(form)
    request.then(() => navigate('/admin/students')).catch((err) => setError(err.message))
  }

  const handleCareerSubmit = (event) => {
    event.preventDefault()
    setError('')
    studentService
      .changeCareer(id, careerForm)
      .then(() => navigate('/admin/students'))
      .catch((err) => setError(err.message))
  }

  return (
    <div>
      <h1>{isEdit ? 'Edit student' : 'New student'}</h1>
      {error && <p>{error}</p>}
      <form onSubmit={handleSubmit}>
        <label>
          RUN
          <input name="studentRun" value={form.studentRun} onChange={handleChange} disabled={isEdit} />
          <small>Enter the RUT as 12345678-k</small>
        </label>
        <label>
          Full name
          <input name="studentFullname" value={form.studentFullname} onChange={handleChange} />
        </label>
        <label>
          Email
          <input type="email" name="studentEmail" value={form.studentEmail} onChange={handleChange} />
        </label>
        {!isEdit && (
          <>
            <label>
              Career
              <select name="careerCode" value={form.careerCode} onChange={handleChange}>
                <option value="">Select a career</option>
                {activeCareers.map((career) => (
                  <option key={career.careerId} value={career.careerCode}>
                    {career.careerCode} - {career.careerName}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Temporary password
              <input type="password" name="password" value={form.password} onChange={handleChange} />
            </label>
          </>
        )}
        <button type="submit">Save</button>
        <Link to="/admin/students">Cancel</Link>
      </form>

      {isEdit && (
        <form onSubmit={handleCareerSubmit}>
          <h2>Change career and study plan</h2>
          <p>Only allowed while the student has no enrollments or academic history.</p>
          <label>
            Career
            <select name="careerCode" value={careerForm.careerCode} onChange={handleCareerChange}>
              <option value="">Select a career</option>
              {activeCareers.map((career) => (
                <option key={career.careerId} value={career.careerCode}>
                  {career.careerCode} - {career.careerName}
                </option>
              ))}
            </select>
          </label>
          <label>
            Study plan
            <select
              name="studyplanIdentifier"
              value={careerForm.studyplanIdentifier}
              onChange={handleCareerChange}
            >
              <option value="">Select a study plan</option>
              {careerStudyplans.map((studyplan) => (
                <option key={studyplan.studyplanId} value={studyplan.studyplanIdentifier}>
                  {studyplan.studyplanIdentifier} ({studyplan.studyplanStatus})
                </option>
              ))}
            </select>
          </label>
          <button type="submit">Change career</button>
        </form>
      )}
    </div>
  )
}

export default AddEditStudent
