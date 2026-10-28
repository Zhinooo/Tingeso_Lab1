import { useEffect, useState } from 'react'
import enrollmentService from '../../services/enrollment.service'

function MyHistory() {
  const [history, setHistory] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    enrollmentService
      .getMyHistory()
      .then((response) => setHistory(response.data))
      .catch((err) => setError(err.message))
  }, [])

  return (
    <div>
      <h1>Academic history</h1>
      {error && <p>{error}</p>}
      <table>
        <thead>
          <tr>
            <th>Period</th>
            <th>Code</th>
            <th>Course</th>
            <th>Final grade</th>
            <th>Result</th>
          </tr>
        </thead>
        <tbody>
          {history.map((enrollment) => (
            <tr key={enrollment.enrollmentId}>
              <td>{`${enrollment.academicYear}-${enrollment.academicSemester}`}</td>
              <td>{enrollment.courseCode}</td>
              <td>{enrollment.courseName}</td>
              <td>{enrollment.finalGrade}</td>
              <td>{enrollment.result}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default MyHistory
