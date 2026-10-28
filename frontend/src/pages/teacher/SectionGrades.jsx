import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import enrollmentService from '../../services/enrollment.service'
import sectionService from '../../services/section.service'

function SectionGrades() {
  const { id } = useParams()
  const [section, setSection] = useState(null)
  const [enrollments, setEnrollments] = useState([])
  const [grades, setGrades] = useState({})
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([sectionService.get(id), enrollmentService.getMySectionEnrollments(id)])
      .then(([sectionResponse, enrollmentsResponse]) => {
        setSection(sectionResponse.data)
        setEnrollments(enrollmentsResponse.data)
        setGrades(
          Object.fromEntries(
            enrollmentsResponse.data.map((enrollment) => [enrollment.enrollmentId, enrollment.finalGrade ?? '']),
          ),
        )
      })
      .catch((err) => setError(err.message))
  }, [id])

  const handleGradeChange = (enrollmentId, value) => {
    setGrades({ ...grades, [enrollmentId]: value })
  }

  const handleSave = (enrollment) => {
    setError('')
    enrollmentService
      .registerGrade(enrollment.enrollmentId, { finalGrade: Number(grades[enrollment.enrollmentId]) })
      .then((response) =>
        setEnrollments((current) =>
          current.map((item) => (item.enrollmentId === enrollment.enrollmentId ? response.data : item)),
        ),
      )
      .catch((err) => setError(err.message))
  }

  return (
    <div>
      <h1>Grades</h1>
      {error && <p>{error}</p>}
      {section && (
        <p>
          {section.courseCode} - {section.courseName} ({`${section.academicYear}-${section.academicSemester}`})
        </p>
      )}
      <table>
        <thead>
          <tr>
            <th>Student RUN</th>
            <th>Final grade (1.0 to 7.0)</th>
            <th>Result</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {enrollments.map((enrollment) => (
            <tr key={enrollment.enrollmentId}>
              <td>{enrollment.studentRun}</td>
              <td>
                <input
                  type="number"
                  min="1"
                  max="7"
                  step="0.1"
                  value={grades[enrollment.enrollmentId] ?? ''}
                  onChange={(event) => handleGradeChange(enrollment.enrollmentId, event.target.value)}
                />
              </td>
              <td>{enrollment.result}</td>
              <td>
                <button onClick={() => handleSave(enrollment)}>Save</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <Link to="/teacher/sections">Back</Link>
    </div>
  )
}

export default SectionGrades
