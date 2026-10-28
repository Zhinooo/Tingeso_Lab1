import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import studentService from '../../services/student.service'

const MANUAL_STATUSES = ['REGULAR', 'DEFERRED', 'TEMPORARY_WITHDRAWAL']

function StudentList() {
  const [students, setStudents] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    studentService
      .getAll()
      .then((response) => setStudents(response.data))
      .catch((err) => setError(err.message))
  }, [])

  const handleStatusChange = (student, newStatus) => {
    setError('')
    studentService
      .changeStatus(student.studentId, newStatus)
      .then((response) =>
        setStudents((current) =>
          current.map((item) => (item.studentId === student.studentId ? response.data : item)),
        ),
      )
      .catch((err) => setError(err.message))
  }

  const handleDelete = (student) => {
    if (!window.confirm(`Delete student ${student.studentFullname}?`)) return
    setError('')
    studentService
      .remove(student.studentId)
      .then(() => setStudents((current) => current.filter((item) => item.studentId !== student.studentId)))
      .catch((err) => setError(err.message))
  }

  return (
    <div>
      <h1>Students</h1>
      {error && <p>{error}</p>}
      <Link to="/admin/students/new">New student</Link>
      <table>
        <thead>
          <tr>
            <th>RUN</th>
            <th>Full name</th>
            <th>Email</th>
            <th>Career</th>
            <th>Study plan</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {students.map((student) => (
            <tr key={student.studentId}>
              <td>{student.studentRun}</td>
              <td>{student.studentFullname}</td>
              <td>{student.studentEmail}</td>
              <td>{student.careerCode}</td>
              <td>{student.studyplanIdentifier}</td>
              <td>
                {MANUAL_STATUSES.includes(student.studentStatus) ? (
                  <select
                    value={student.studentStatus}
                    onChange={(event) => handleStatusChange(student, event.target.value)}
                  >
                    {MANUAL_STATUSES.map((status) => (
                      <option key={status} value={status}>
                        {status}
                      </option>
                    ))}
                  </select>
                ) : (
                  student.studentStatus
                )}
              </td>
              <td>
                <Link to={`/admin/students/${student.studentId}/history`}>History</Link>
                <Link to={`/admin/students/${student.studentId}/edit`}>Edit</Link>
                <button onClick={() => handleDelete(student)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default StudentList
