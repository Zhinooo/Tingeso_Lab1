import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import sectionService from '../../services/section.service'

function MySections() {
  const [sections, setSections] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    sectionService
      .getMine()
      .then((response) => setSections(response.data))
      .catch((err) => setError(err.message))
  }, [])

  return (
    <div>
      <h1>My sections</h1>
      {error && <p>{error}</p>}
      <table>
        <thead>
          <tr>
            <th>Period</th>
            <th>Course</th>
            <th>Capacity</th>
            <th>Schedule</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {sections.map((section) => (
            <tr key={section.sectionId}>
              <td>{`${section.academicYear}-${section.academicSemester}`}</td>
              <td>
                {section.courseCode} - {section.courseName}
              </td>
              <td>{section.sectionCapacity}</td>
              <td>{section.scheduleBlocks.join(', ')}</td>
              <td>
                <Link to={`/teacher/sections/${section.sectionId}`}>Students and grades</Link>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default MySections
