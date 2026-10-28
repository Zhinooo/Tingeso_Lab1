import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import courseService from '../../services/course.service'
import studyplanService from '../../services/studyplan.service'

function CourseList() {
  const [searchParams, setSearchParams] = useSearchParams()
  const studyplanId = searchParams.get('studyplanId') ?? ''
  const [studyplans, setStudyplans] = useState([])
  const [courses, setCourses] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    studyplanService
      .getAll()
      .then((response) => setStudyplans(response.data))
      .catch((err) => setError(err.message))
  }, [])

  useEffect(() => {
    const request = studyplanId ? courseService.getByStudyplan(studyplanId) : courseService.getAll()
    request.then((response) => setCourses(response.data)).catch((err) => setError(err.message))
  }, [studyplanId])

  const handleStudyplanChange = (event) => {
    const value = event.target.value
    setSearchParams(value ? { studyplanId: value } : {})
  }

  const handleDelete = (course) => {
    if (!window.confirm(`Delete course ${course.courseCode}?`)) return
    setError('')
    courseService
      .remove(course.courseId)
      .then(() => setCourses((current) => current.filter((item) => item.courseId !== course.courseId)))
      .catch((err) => setError(err.message))
  }

  return (
    <div>
      <h1>Courses</h1>
      {error && <p>{error}</p>}
      <label>
        Study plan
        <select value={studyplanId} onChange={handleStudyplanChange}>
          <option value="">All</option>
          {studyplans.map((studyplan) => (
            <option key={studyplan.studyplanId} value={studyplan.studyplanId}>
              {studyplan.careerCode} - {studyplan.studyplanIdentifier}
            </option>
          ))}
        </select>
      </label>
      <Link to={studyplanId ? `/admin/courses/new?studyplanId=${studyplanId}` : '/admin/courses/new'}>
        New course
      </Link>
      <table>
        <thead>
          <tr>
            <th>Semester</th>
            <th>Code</th>
            <th>Name</th>
            <th>TEL</th>
            <th>SCT</th>
            <th>Study plan</th>
            <th>Prerequisites</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {courses.map((course) => (
            <tr key={course.courseId}>
              <td>{course.semester}</td>
              <td>{course.courseCode}</td>
              <td>{course.courseName}</td>
              <td>{`${course.theoryHours}-${course.exerciseHours}-${course.labHours}`}</td>
              <td>{course.sct}</td>
              <td>{course.studyplanIdentifier}</td>
              <td>{course.prerequisiteCodes.join(', ')}</td>
              <td>
                <Link to={`/admin/courses/${course.courseId}/edit`}>Edit</Link>
                <button onClick={() => handleDelete(course)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default CourseList
