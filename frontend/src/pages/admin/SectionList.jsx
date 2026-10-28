import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import academicPeriodService from '../../services/academicPeriod.service'
import sectionService from '../../services/section.service'

function SectionList() {
  const [searchParams, setSearchParams] = useSearchParams()
  const academicPeriodId = searchParams.get('academicPeriodId') ?? ''
  const [periods, setPeriods] = useState([])
  const [sections, setSections] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    academicPeriodService
      .getAll()
      .then((response) => setPeriods(response.data))
      .catch((err) => setError(err.message))
  }, [])

  useEffect(() => {
    const request = academicPeriodId
      ? sectionService.getByAcademicPeriod(academicPeriodId)
      : sectionService.getAll()
    request.then((response) => setSections(response.data)).catch((err) => setError(err.message))
  }, [academicPeriodId])

  const handlePeriodChange = (event) => {
    const value = event.target.value
    setSearchParams(value ? { academicPeriodId: value } : {})
  }

  const handleDelete = (section) => {
    if (!window.confirm(`Delete section of ${section.courseCode}?`)) return
    setError('')
    sectionService
      .remove(section.sectionId)
      .then(() => setSections((current) => current.filter((item) => item.sectionId !== section.sectionId)))
      .catch((err) => setError(err.message))
  }

  return (
    <div>
      <h1>Sections</h1>
      {error && <p>{error}</p>}
      <label>
        Academic period
        <select value={academicPeriodId} onChange={handlePeriodChange}>
          <option value="">All</option>
          {periods.map((period) => (
            <option key={period.academicPeriodId} value={period.academicPeriodId}>
              {`${period.academicYear}-${period.academicSemester}`} ({period.academicPeriodStatus})
            </option>
          ))}
        </select>
      </label>
      <Link to={academicPeriodId ? `/admin/sections/new?academicPeriodId=${academicPeriodId}` : '/admin/sections/new'}>
        New section
      </Link>
      <table>
        <thead>
          <tr>
            <th>Period</th>
            <th>Course</th>
            <th>Teacher</th>
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
              <td>{section.teacherFullname}</td>
              <td>{section.sectionCapacity}</td>
              <td>{section.scheduleBlocks.join(', ')}</td>
              <td>
                <Link to={`/admin/sections/${section.sectionId}/edit`}>Edit</Link>
                <button onClick={() => handleDelete(section)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default SectionList
