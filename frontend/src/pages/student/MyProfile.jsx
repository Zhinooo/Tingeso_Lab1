import { useEffect, useState } from 'react'
import LinearProgress from '@mui/material/LinearProgress'
import ErrorAlert from '../../components/ErrorAlert'
import PageHeader from '../../components/PageHeader'
import ProfileSummary from '../../components/profile/ProfileSummary'
import StudyplanOverview from '../../components/profile/StudyplanOverview'
import StudyplanProgress from '../../components/profile/StudyplanProgress'
import careerService from '../../services/career.service'
import courseService from '../../services/course.service'
import enrollmentService from '../../services/enrollment.service'
import studentService from '../../services/student.service'
import studyplanService from '../../services/studyplan.service'
import { buildCourseStatuses, summarizeProgress } from '../../utils/progress'

function MyProfile() {
  const [student, setStudent] = useState(null)
  const [careerName, setCareerName] = useState('')
  const [courses, setCourses] = useState([])
  const [courseStatuses, setCourseStatuses] = useState({})
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([
      studentService.getMine(),
      studyplanService.getAll(),
      careerService.getAll(),
      enrollmentService.getMyHistory(),
      enrollmentService.getMine(),
    ])
      .then(([studentResponse, studyplansResponse, careersResponse, historyResponse, enrollmentsResponse]) => {
        const me = studentResponse.data
        setStudent(me)
        setCourseStatuses(buildCourseStatuses(historyResponse.data, enrollmentsResponse.data))
        setCareerName(careersResponse.data.find((career) => career.careerCode === me.careerCode)?.careerName)
        const studyplan = studyplansResponse.data.find(
          (item) => item.careerCode === me.careerCode && item.studyplanIdentifier === me.studyplanIdentifier,
        )
        if (!studyplan) {
          throw new Error('No se encontró tu plan de estudio.')
        }
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
          <StudyplanOverview
            courses={courses}
            courseStatuses={courseStatuses}
            action={<StudyplanProgress progress={summarizeProgress(courses, courseStatuses)} />}
          />
        </>
      )}
    </>
  )
}

export default MyProfile
