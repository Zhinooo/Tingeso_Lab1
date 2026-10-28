import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import academicPeriodService from '../../services/academicPeriod.service'
import courseService from '../../services/course.service'
import sectionService from '../../services/section.service'
import teacherService from '../../services/teacher.service'

const DAYS = ['L', 'M', 'W', 'J', 'V']
const MODULES = [1, 2, 3, 4, 5, 6]

const emptyForm = {
  courseId: '',
  academicPeriodId: '',
  teacherId: '',
  sectionCapacity: '',
  scheduleBlocks: [],
}

const toScheduleBlock = (code) => ({ scheduleDay: code[0], module: Number(code.slice(1)) })

function AddEditSection() {
  const { id } = useParams()
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const [form, setForm] = useState({ ...emptyForm, academicPeriodId: searchParams.get('academicPeriodId') ?? '' })
  const [courses, setCourses] = useState([])
  const [periods, setPeriods] = useState([])
  const [teachers, setTeachers] = useState([])
  const [error, setError] = useState('')
  const isEdit = Boolean(id)

  useEffect(() => {
    Promise.all([courseService.getAll(), academicPeriodService.getAll(), teacherService.getAll()])
      .then(([coursesResponse, periodsResponse, teachersResponse]) => {
        setCourses(coursesResponse.data)
        setPeriods(periodsResponse.data)
        setTeachers(teachersResponse.data)
      })
      .catch((err) => setError(err.message))
  }, [])

  useEffect(() => {
    if (!id) return
    sectionService
      .get(id)
      .then((response) => {
        const section = response.data
        setForm({
          courseId: section.courseId,
          academicPeriodId: section.academicPeriodId,
          teacherId: section.teacherId,
          sectionCapacity: section.sectionCapacity,
          scheduleBlocks: section.scheduleBlocks,
        })
      })
      .catch((err) => setError(err.message))
  }, [id])

  const availableTeachers = teachers.filter(
    (teacher) => teacher.teacherStatus === 'ACTIVE' || teacher.teacherId === Number(form.teacherId),
  )
  const availablePeriods = periods.filter(
    (period) => period.academicPeriodStatus === 'OPEN' || period.academicPeriodId === Number(form.academicPeriodId),
  )
  const selectedCourse = courses.find((course) => course.courseId === Number(form.courseId))
  const requiredBlocks = selectedCourse
    ? (selectedCourse.theoryHours + selectedCourse.exerciseHours + selectedCourse.labHours) / 2
    : null

  const handleChange = (event) => {
    const { name, value } = event.target
    setForm({ ...form, [name]: value })
  }

  const toggleBlock = (code) => {
    const blocks = form.scheduleBlocks
    setForm({
      ...form,
      scheduleBlocks: blocks.includes(code) ? blocks.filter((item) => item !== code) : [...blocks, code],
    })
  }

  const handleSubmit = (event) => {
    event.preventDefault()
    setError('')
    const scheduleBlocks = form.scheduleBlocks.map(toScheduleBlock)
    const request = isEdit
      ? sectionService.update(id, {
          teacherId: Number(form.teacherId),
          sectionCapacity: Number(form.sectionCapacity),
          scheduleBlocks,
        })
      : sectionService.create({
          courseId: Number(form.courseId),
          academicPeriodId: Number(form.academicPeriodId),
          teacherId: Number(form.teacherId),
          sectionCapacity: Number(form.sectionCapacity),
          scheduleBlocks,
        })
    request
      .then(() => navigate(`/admin/sections?academicPeriodId=${form.academicPeriodId}`))
      .catch((err) => setError(err.message))
  }

  return (
    <div>
      <h1>{isEdit ? 'Edit section' : 'New section'}</h1>
      {error && <p>{error}</p>}
      <form onSubmit={handleSubmit}>
        <label>
          Academic period
          <select name="academicPeriodId" value={form.academicPeriodId} onChange={handleChange} disabled={isEdit}>
            <option value="">Select a period</option>
            {availablePeriods.map((period) => (
              <option key={period.academicPeriodId} value={period.academicPeriodId}>
                {`${period.academicYear}-${period.academicSemester}`}
              </option>
            ))}
          </select>
        </label>
        <label>
          Course
          <select name="courseId" value={form.courseId} onChange={handleChange} disabled={isEdit}>
            <option value="">Select a course</option>
            {courses.map((course) => (
              <option key={course.courseId} value={course.courseId}>
                {course.courseCode} - {course.courseName} ({course.studyplanIdentifier})
              </option>
            ))}
          </select>
        </label>
        <label>
          Teacher
          <select name="teacherId" value={form.teacherId} onChange={handleChange}>
            <option value="">Select a teacher</option>
            {availableTeachers.map((teacher) => (
              <option key={teacher.teacherId} value={teacher.teacherId}>
                {teacher.teacherFullname}
              </option>
            ))}
          </select>
        </label>
        <label>
          Capacity (1 to 60)
          <input
            type="number"
            min="1"
            max="60"
            name="sectionCapacity"
            value={form.sectionCapacity}
            onChange={handleChange}
          />
        </label>
        <fieldset>
          <legend>
            Schedule: {form.scheduleBlocks.length} selected
            {requiredBlocks !== null && ` of ${requiredBlocks} required`}
          </legend>
          <table>
            <thead>
              <tr>
                <th>Module</th>
                {DAYS.map((day) => (
                  <th key={day}>{day}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {MODULES.map((module) => (
                <tr key={module}>
                  <td>M{module}</td>
                  {DAYS.map((day) => {
                    const code = `${day}${module}`
                    return (
                      <td key={code}>
                        <input
                          type="checkbox"
                          checked={form.scheduleBlocks.includes(code)}
                          onChange={() => toggleBlock(code)}
                        />
                      </td>
                    )
                  })}
                </tr>
              ))}
            </tbody>
          </table>
        </fieldset>
        <button type="submit">Save</button>
        <Link to="/admin/sections">Cancel</Link>
      </form>
    </div>
  )
}

export default AddEditSection
