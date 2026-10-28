import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import academicPeriodService from '../../services/academicPeriod.service'
import enrollmentService from '../../services/enrollment.service'

function EnrollCourses() {
  const navigate = useNavigate()
  const [periods, setPeriods] = useState([])
  const [academicPeriodId, setAcademicPeriodId] = useState('')
  const [available, setAvailable] = useState([])
  const [selected, setSelected] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    academicPeriodService
      .getAll()
      .then((response) => {
        const openPeriods = response.data.filter((period) => period.academicPeriodStatus === 'OPEN')
        setPeriods(openPeriods)
        if (openPeriods.length > 0) {
          setAcademicPeriodId(String(openPeriods[0].academicPeriodId))
        }
      })
      .catch((err) => setError(err.message))
  }, [])

  useEffect(() => {
    if (!academicPeriodId) return
    enrollmentService
      .getMyAvailable(academicPeriodId)
      .then((response) => setAvailable(response.data))
      .catch((err) => setError(err.message))
  }, [academicPeriodId])

  const handlePeriodChange = (event) => {
    setAcademicPeriodId(event.target.value)
    setSelected([])
  }

  const toggleCourse = (courseId) => {
    setSelected(selected.includes(courseId) ? selected.filter((item) => item !== courseId) : [...selected, courseId])
  }

  const handleSubmit = (event) => {
    event.preventDefault()
    setError('')
    enrollmentService
      .create({ academicPeriodId: Number(academicPeriodId), courseIds: selected })
      .then(() => navigate('/student/enrollments'))
      .catch((err) => setError(err.message))
  }

  return (
    <div>
      <h1>Enroll courses</h1>
      {error && <p>{error}</p>}
      <label>
        Academic period
        <select value={academicPeriodId} onChange={handlePeriodChange}>
          {periods.map((period) => (
            <option key={period.academicPeriodId} value={period.academicPeriodId}>
              {`${period.academicYear}-${period.academicSemester}`}
            </option>
          ))}
        </select>
      </label>
      <form onSubmit={handleSubmit}>
        <table>
          <thead>
            <tr>
              <th />
              <th>Semester</th>
              <th>Code</th>
              <th>Course</th>
              <th>Teacher</th>
              <th>Schedule</th>
              <th>Available seats</th>
            </tr>
          </thead>
          <tbody>
            {available.map((course) => (
              <tr key={course.courseId}>
                <td>
                  <input
                    type="checkbox"
                    checked={selected.includes(course.courseId)}
                    onChange={() => toggleCourse(course.courseId)}
                    disabled={course.availableSeats === 0}
                  />
                </td>
                <td>{course.semester}</td>
                <td>{course.courseCode}</td>
                <td>{course.courseName}</td>
                <td>{course.teacherFullname}</td>
                <td>{course.scheduleBlocks.join(', ')}</td>
                <td>{`${course.availableSeats} / ${course.sectionCapacity}`}</td>
              </tr>
            ))}
          </tbody>
        </table>
        <p>{selected.length} selected (between 3 and 6)</p>
        <button type="submit" disabled={selected.length === 0}>
          Enroll
        </button>
      </form>
    </div>
  )
}

export default EnrollCourses
