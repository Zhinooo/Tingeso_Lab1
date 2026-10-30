import { useEffect, useState } from 'react'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import Divider from '@mui/material/Divider'
import ErrorAlert from '../../components/ErrorAlert'
import FormCard from '../../components/FormCard'
import PageHeader from '../../components/PageHeader'
import CourseFormFields from '../../components/courses/CourseFormFields'
import CourseWorkloadFields from '../../components/courses/CourseWorkloadFields'
import PrerequisitePicker from '../../components/courses/PrerequisitePicker'
import courseService from '../../services/course.service'
import studyplanService from '../../services/studyplan.service'

const emptyForm = {
  studyplanId: '',
  semester: 1,
  courseCode: '',
  courseName: '',
  theoryHours: 0,
  exerciseHours: 0,
  labHours: 0,
  sct: 1,
  prerequisiteCodes: [],
}

function AddEditCourse() {
  const { id } = useParams()
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const [form, setForm] = useState({ ...emptyForm, studyplanId: searchParams.get('studyplanId') ?? '' })
  const [studyplans, setStudyplans] = useState([])
  const [planCourses, setPlanCourses] = useState([])
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)
  const isEdit = Boolean(id)

  useEffect(() => {
    Promise.all([studyplanService.getAll(), id ? courseService.get(id) : null])
      .then(([studyplansResponse, courseResponse]) => {
        setStudyplans(studyplansResponse.data)
        if (!courseResponse) return
        const course = courseResponse.data
        setForm({
          studyplanId: String(course.studyplanId),
          semester: course.semester,
          courseCode: course.courseCode,
          courseName: course.courseName,
          theoryHours: course.theoryHours,
          exerciseHours: course.exerciseHours,
          labHours: course.labHours,
          sct: course.sct,
          prerequisiteCodes: course.prerequisiteCodes,
        })
      })
      .catch((err) => setError(err.message))
  }, [id])

  useEffect(() => {
    if (!form.studyplanId) return
    courseService
      .getByStudyplan(form.studyplanId)
      .then((response) => setPlanCourses(response.data))
      .catch((err) => setError(err.message))
  }, [form.studyplanId])

  const isValidPrerequisite = (course, semester) =>
    course.semester < Number(semester) && course.courseId !== Number(id)

  const prerequisiteOptions = planCourses.filter((course) => isValidPrerequisite(course, form.semester))
  const listUrl = form.studyplanId ? `/admin/courses?studyplanId=${form.studyplanId}` : '/admin/courses'

  const handleChange = (event) => {
    const { name, value } = event.target
    const next = { ...form, [name]: value }
    if (name === 'studyplanId') {
      next.prerequisiteCodes = []
    }
    if (name === 'semester') {
      next.prerequisiteCodes = form.prerequisiteCodes.filter((code) =>
        planCourses.some((course) => course.courseCode === code && isValidPrerequisite(course, value)),
      )
    }
    setForm(next)
  }

  const togglePrerequisite = (code) => {
    const codes = form.prerequisiteCodes
    setForm({
      ...form,
      prerequisiteCodes: codes.includes(code) ? codes.filter((item) => item !== code) : [...codes, code],
    })
  }

  const handleSubmit = (event) => {
    event.preventDefault()
    setError('')
    setSaving(true)
    const data = {
      ...form,
      studyplanId: Number(form.studyplanId),
      semester: Number(form.semester),
      theoryHours: Number(form.theoryHours),
      exerciseHours: Number(form.exerciseHours),
      labHours: Number(form.labHours),
      sct: Number(form.sct),
    }
    const request = isEdit ? courseService.update(id, data) : courseService.create(data)
    request
      .then(() => navigate(listUrl))
      .catch((err) => {
        setError(err.message)
        setSaving(false)
      })
  }

  return (
    <>
      <PageHeader title={isEdit ? 'Edit course' : 'New course'} backTo={listUrl} />
      <ErrorAlert message={error} />
      <FormCard onSubmit={handleSubmit} cancelTo={listUrl} saving={saving}>
        <CourseFormFields form={form} studyplans={studyplans} isEdit={isEdit} onChange={handleChange} />
        <Divider sx={{ my: 3 }} />
        <CourseWorkloadFields form={form} onChange={handleChange} />
        <Divider sx={{ my: 3 }} />
        <PrerequisitePicker
          options={prerequisiteOptions}
          selected={form.prerequisiteCodes}
          hasStudyplan={Boolean(form.studyplanId)}
          onToggle={togglePrerequisite}
        />
      </FormCard>
    </>
  )
}

export default AddEditCourse
