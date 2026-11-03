import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import Box from '@mui/material/Box'
import LinearProgress from '@mui/material/LinearProgress'
import Paper from '@mui/material/Paper'
import EmptyState from '../../components/EmptyState'
import ErrorAlert from '../../components/ErrorAlert'
import PageHeader from '../../components/PageHeader'
import StatusChip from '../../components/StatusChip'
import StudyplanFilter from '../../components/StudyplanFilter'
import CareerStudentTable from '../../components/careers/CareerStudentTable'
import StudyplanOverview from '../../components/profile/StudyplanOverview'
import { FINAL_STUDENT_STATUSES } from '../../constants/students'
import careerService from '../../services/career.service'
import courseService from '../../services/course.service'
import studentService from '../../services/student.service'
import studyplanService from '../../services/studyplan.service'

function CareerDetail() {
  const { id } = useParams()
  const [career, setCareer] = useState(null)
  const [studyplans, setStudyplans] = useState([])
  const [studyplanId, setStudyplanId] = useState('')
  const [courses, setCourses] = useState([])
  const [students, setStudents] = useState([])
  const [includeFinished, setIncludeFinished] = useState(false)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([careerService.get(id), studyplanService.getAll(), studentService.getAll()])
      .then(([careerResponse, studyplansResponse, studentsResponse]) => {
        const loadedCareer = careerResponse.data
        const careerStudyplans = studyplansResponse.data.filter(
          (studyplan) => studyplan.careerCode === loadedCareer.careerCode,
        )
        const defaultStudyplan =
          careerStudyplans.find((studyplan) => studyplan.studyplanStatus === 'CURRENT') ?? careerStudyplans[0]
        setCareer(loadedCareer)
        setStudyplans(careerStudyplans)
        setStudyplanId(defaultStudyplan ? String(defaultStudyplan.studyplanId) : '')
        setStudents(studentsResponse.data.filter((student) => student.careerCode === loadedCareer.careerCode))
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [id])

  useEffect(() => {
    if (!studyplanId) return
    courseService
      .getByStudyplan(studyplanId)
      .then((response) => setCourses(response.data))
      .catch((err) => setError(err.message))
  }, [studyplanId])

  const visibleStudents = students
    .filter((student) => includeFinished || !FINAL_STUDENT_STATUSES.includes(student.studentStatus))
    .sort((a, b) => a.studentFullname.localeCompare(b.studentFullname))

  return (
    <>
      <PageHeader
        title={career ? `${career.careerCode} · ${career.careerName}` : 'Carrera'}
        subtitle={career?.careerDescription}
        backTo="/admin/careers"
        action={career && <StatusChip status={career.careerStatus} size="medium" />}
      />
      <ErrorAlert message={error} />
      {loading && <LinearProgress sx={{ mb: 2 }} />}
      {career && (
        <>
          <Box sx={{ mb: 4 }}>
            {studyplans.length === 0 ? (
              <Paper>
                <EmptyState
                  message="Esta carrera aún no tiene planes de estudio"
                  hint="Crea un plan en Planes de estudio para ver su malla."
                />
              </Paper>
            ) : (
              <StudyplanOverview
                courses={courses}
                action={
                  <StudyplanFilter
                    studyplans={studyplans}
                    value={studyplanId}
                    onChange={setStudyplanId}
                    allLabel={null}
                  />
                }
              />
            )}
          </Box>
          <CareerStudentTable
            students={visibleStudents}
            totalCount={students.length}
            includeFinished={includeFinished}
            onIncludeFinishedChange={setIncludeFinished}
          />
        </>
      )}
    </>
  )
}

export default CareerDetail
