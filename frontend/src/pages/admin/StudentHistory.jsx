import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import enrollmentService from '../../services/enrollment.service'
import studentService from '../../services/student.service'

function StudentHistory() {
  const { id } = useParams()
  const [student, setStudent] = useState(null)
  const [history, setHistory] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([studentService.get(id), enrollmentService.getStudentHistory(id)])
      .then(([studentResponse, historyResponse]) => {
        setStudent(studentResponse.data)
        setHistory(historyResponse.data)
      })
      .catch((err) => setError(err.message))
  }, [id])

  return (
    <div>
      <h1>Academic history</h1>
      {error && <p>{error}</p>}
      {student && (
        <p>
          {student.studentRun} - {student.studentFullname} ({student.careerCode} / {student.studyplanIdentifier},{' '}
          {student.studentStatus})
        </p>
      )}
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
      <Link to="/admin/students">Back</Link>
    </div>
  )
}

export default StudentHistory
