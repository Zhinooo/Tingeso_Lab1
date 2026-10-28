import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import courseService from '../../services/course.service'
import studyplanService from '../../services/studyplan.service'

const emptyForm = {
  courseCode: '',
  courseName: '',
  semester: '',
  theoryHours: '',
  exerciseHours: '',
  labHours: '',
  sct: '',
  studyplanId: '',
  prerequisiteCodes: [],
}

function AddEditCourse() {
  const { id } = useParams()
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const [form, setForm] = useState({ ...emptyForm, studyplanId: searchParams.get('studyplanId') ?? '' })
  const [studyplans, setStudyplans] = useState([])
  const [planCourses, setPlanCourses] = useState([])
  const [error, setError] = useState('')
  const isEdit = Boolean(id)

  useEffect(() => {
    studyplanService
      .getAll()
      .then((response) => setStudyplans(response.data))
      .catch((err) => setError(err.message))
  }, [])

  useEffect(() => {
    if (!id) return
    courseService
      .get(id)
      .then((response) => {
        const course = response.data
        setForm({
          courseCode: course.courseCode,
          courseName: course.courseName,
          semester: course.semester,
          theoryHours: course.theoryHours,
          exerciseHours: course.exerciseHours,
          labHours: course.labHours,
          sct: course.sct,
          studyplanId: course.studyplanId,
          prerequisiteCodes: course.prerequisiteCodes,
        })
      })
      .catch((err) => setError(err.message))
  }, [id])

  useEffect(() => {
    if (!form.studyplanId) return
    courseService
      .getByStudyplan(form.studyplanId)
      .then((response) => setPlanCourses(response.data))
      .catch((err) => setError(err.message))
  }, [form.studyplanId])

  const prerequisiteOptions = planCourses.filter(
    (course) =>
      course.studyplanId === Number(form.studyplanId) &&
      course.semester < Number(form.semester) &&
      course.courseCode !== form.courseCode,
  )

  const handleChange = (event) => {
    const { name, value } = event.target
    setForm({ ...form, [name]: value })
  }

  const togglePrerequisite = (code) => {
    const codes = form.prerequisiteCodes
    setForm({
      ...form,
      prerequisiteCodes: codes.includes(code) ? codes.filter((item) => item !== code) : [...codes, code],
    })
  }

  const handleSubmit = (event) => {
    event.preventDefault()
    setError('')
    const data = {
      ...form,
      semester: Number(form.semester),
      theoryHours: Number(form.theoryHours),
      exerciseHours: Number(form.exerciseHours),
      labHours: Number(form.labHours),
      sct: Number(form.sct),
      studyplanId: Number(form.studyplanId),
    }
    const request = isEdit ? courseService.update(id, data) : courseService.create(data)
    request
      .then(() => navigate(`/admin/courses?studyplanId=${data.studyplanId}`))
      .catch((err) => setError(err.message))
  }

  return (
    <div>
      <h1>{isEdit ? 'Edit course' : 'New course'}</h1>
      {error && <p>{error}</p>}
      <form onSubmit={handleSubmit}>
        <label>
          Study plan
          <select name="studyplanId" value={form.studyplanId} onChange={handleChange} disabled={isEdit}>
            <option value="">Select a study plan</option>
            {studyplans.map((studyplan) => (
              <option key={studyplan.studyplanId} value={studyplan.studyplanId}>
                {studyplan.careerCode} - {studyplan.studyplanIdentifier}
              </option>
            ))}
          </select>
        </label>
        <label>
          Code
          <input name="courseCode" value={form.courseCode} onChange={handleChange} />
        </label>
        <label>
          Name
          <input name="courseName" value={form.courseName} onChange={handleChange} />
        </label>
        <label>
          Semester
          <select name="semester" value={form.semester} onChange={handleChange}>
            <option value="">Select</option>
            {[1, 2, 3, 4].map((semester) => (
              <option key={semester} value={semester}>
                {semester}
              </option>
            ))}
          </select>
        </label>
        <label>
          Theory hours
          <input type="number" min="0" name="theoryHours" value={form.theoryHours} onChange={handleChange} />
        </label>
        <label>
          Exercise hours
          <input type="number" min="0" name="exerciseHours" value={form.exerciseHours} onChange={handleChange} />
        </label>
        <label>
          Lab hours
          <input type="number" min="0" name="labHours" value={form.labHours} onChange={handleChange} />
        </label>
        <label>
          SCT
          <input type="number" min="1" max="7" name="sct" value={form.sct} onChange={handleChange} />
        </label>
        <fieldset>
          <legend>Prerequisites (max 3, from earlier semesters)</legend>
          {prerequisiteOptions.map((course) => (
            <label key={course.courseId}>
              <input
                type="checkbox"
                checked={form.prerequisiteCodes.includes(course.courseCode)}
                onChange={() => togglePrerequisite(course.courseCode)}
              />
              {course.courseCode} - {course.courseName}
            </label>
          ))}
        </fieldset>
        <button type="submit">Save</button>
        <Link to="/admin/courses">Cancel</Link>
      </form>
    </div>
  )
}

export default AddEditCourse
