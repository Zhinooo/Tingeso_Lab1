import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import enrollmentService from '../../services/enrollment.service'

function MyEnrollments() {
  const [enrollments, setEnrollments] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    enrollmentService
      .getMine()
      .then((response) => setEnrollments(response.data))
      .catch((err) => setError(err.message))
  }, [])

  const handleCancel = (enrollment) => {
    if (!window.confirm(`Cancel enrollment in ${enrollment.courseName}?`)) return
    setError('')
    enrollmentService
      .cancel(enrollment.enrollmentId)
      .then(() =>
        setEnrollments((current) => current.filter((item) => item.enrollmentId !== enrollment.enrollmentId)),
      )
      .catch((err) => setError(err.message))
  }

  return (
    <div>
      <h1>My enrollments</h1>
      {error && <p>{error}</p>}
      <Link to="/student/enroll">Enroll courses</Link>
      <table>
        <thead>
          <tr>
            <th>Period</th>
            <th>Code</th>
            <th>Course</th>
            <th>Date</th>
            <th>Final grade</th>
            <th>Result</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {enrollments.map((enrollment) => (
            <tr key={enrollment.enrollmentId}>
              <td>{`${enrollment.academicYear}-${enrollment.academicSemester}`}</td>
              <td>{enrollment.courseCode}</td>
              <td>{enrollment.courseName}</td>
              <td>{enrollment.enrollmentDate}</td>
              <td>{enrollment.finalGrade}</td>
              <td>{enrollment.result}</td>
              <td>
                <button onClick={() => handleCancel(enrollment)}>Cancel</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default MyEnrollments
