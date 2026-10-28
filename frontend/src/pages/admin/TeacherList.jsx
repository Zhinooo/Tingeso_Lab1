import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import teacherService from '../../services/teacher.service'

function TeacherList() {
  const [teachers, setTeachers] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    teacherService
      .getAll()
      .then((response) => setTeachers(response.data))
      .catch((err) => setError(err.message))
  }, [])

  const handleToggleStatus = (teacher) => {
    const newStatus = teacher.teacherStatus === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
    setError('')
    teacherService
      .changeStatus(teacher.teacherId, newStatus)
      .then((response) =>
        setTeachers((current) =>
          current.map((item) => (item.teacherId === teacher.teacherId ? response.data : item)),
        ),
      )
      .catch((err) => setError(err.message))
  }

  const handleDelete = (teacher) => {
    if (!window.confirm(`Delete teacher ${teacher.teacherFullname}?`)) return
    setError('')
    teacherService
      .remove(teacher.teacherId)
      .then(() => setTeachers((current) => current.filter((item) => item.teacherId !== teacher.teacherId)))
      .catch((err) => setError(err.message))
  }

  return (
    <div>
      <h1>Teachers</h1>
      {error && <p>{error}</p>}
      <Link to="/admin/teachers/new">New teacher</Link>
      <table>
        <thead>
          <tr>
            <th>RUN</th>
            <th>Full name</th>
            <th>Email</th>
            <th>Professional title</th>
            <th>Degree</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {teachers.map((teacher) => (
            <tr key={teacher.teacherId}>
              <td>{teacher.teacherRun}</td>
              <td>{teacher.teacherFullname}</td>
              <td>{teacher.teacherEmail}</td>
              <td>{teacher.teacherProtitle}</td>
              <td>{teacher.teacherDegree}</td>
              <td>{teacher.teacherStatus}</td>
              <td>
                <Link to={`/admin/teachers/${teacher.teacherId}/edit`}>Edit</Link>
                <button onClick={() => handleToggleStatus(teacher)}>
                  {teacher.teacherStatus === 'ACTIVE' ? 'Deactivate' : 'Activate'}
                </button>
                <button onClick={() => handleDelete(teacher)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default TeacherList
