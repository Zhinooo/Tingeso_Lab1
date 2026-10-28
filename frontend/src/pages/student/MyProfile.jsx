import { useEffect, useState } from 'react'
import courseService from '../../services/course.service'
import studentService from '../../services/student.service'
import studyplanService from '../../services/studyplan.service'

function MyProfile() {
  const [student, setStudent] = useState(null)
  const [courses, setCourses] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([studentService.getMine(), studyplanService.getAll()])
      .then(([studentResponse, studyplansResponse]) => {
        const me = studentResponse.data
        setStudent(me)
        const studyplan = studyplansResponse.data.find(
          (item) => item.careerCode === me.careerCode && item.studyplanIdentifier === me.studyplanIdentifier,
        )
        return courseService.getByStudyplan(studyplan.studyplanId)
      })
      .then((response) => setCourses(response.data))
      .catch((err) => setError(err.message))
  }, [])

  return (
    <div>
      <h1>My profile</h1>
      {error && <p>{error}</p>}
      {student && (
        <ul>
          <li>RUN: {student.studentRun}</li>
          <li>Full name: {student.studentFullname}</li>
          <li>Email: {student.studentEmail}</li>
          <li>Career: {student.careerCode}</li>
          <li>Study plan: {student.studyplanIdentifier}</li>
          <li>Status: {student.studentStatus}</li>
        </ul>
      )}
      <h2>Study plan</h2>
      <table>
        <thead>
          <tr>
            <th>Semester</th>
            <th>Code</th>
            <th>Course</th>
            <th>TEL</th>
            <th>SCT</th>
            <th>Prerequisites</th>
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
              <td>{course.prerequisiteCodes.join(', ')}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default MyProfile
