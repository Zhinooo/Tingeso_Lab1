import { useEffect, useState } from 'react'
import LinearProgress from '@mui/material/LinearProgress'
import ErrorAlert from '../../components/ErrorAlert'
import PageHeader from '../../components/PageHeader'
import ProfileSummary from '../../components/profile/ProfileSummary'
import StudyplanOverview from '../../components/profile/StudyplanOverview'
import careerService from '../../services/career.service'
import courseService from '../../services/course.service'
import studentService from '../../services/student.service'
import studyplanService from '../../services/studyplan.service'

function MyProfile() {
  const [student, setStudent] = useState(null)
  const [careerName, setCareerName] = useState('')
  const [courses, setCourses] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([studentService.getMine(), studyplanService.getAll(), careerService.getAll()])
      .then(([studentResponse, studyplansResponse, careersResponse]) => {
        const me = studentResponse.data
        setStudent(me)
        setCareerName(careersResponse.data.find((career) => career.careerCode === me.careerCode)?.careerName)
        const studyplan = studyplansResponse.data.find(
          (item) => item.careerCode === me.careerCode && item.studyplanIdentifier === me.studyplanIdentifier,
        )
        return courseService.getByStudyplan(studyplan.studyplanId)
      })
      .then((response) => setCourses(response.data))
      .catch((err) => setError(err.message))
  }, [])

  return (
    <>
      <PageHeader title="Mi perfil" />
      <ErrorAlert message={error} />
      {!student && !error && <LinearProgress />}
      {student && (
        <>
          <ProfileSummary student={student} careerName={careerName} />
          <StudyplanOverview courses={courses} />
        </>
      )}
    </>
  )
}

export default MyProfile
