import { useEffect, useState } from 'react'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import Divider from '@mui/material/Divider'
import ErrorAlert from '../../components/ErrorAlert'
import FormCard from '../../components/FormCard'
import PageHeader from '../../components/PageHeader'
import ScheduleGrid from '../../components/sections/ScheduleGrid'
import SectionFormFields from '../../components/sections/SectionFormFields'
import { HOURS_PER_BLOCK } from '../../constants/sections'
import academicPeriodService from '../../services/academicPeriod.service'
import courseService from '../../services/course.service'
import sectionService from '../../services/section.service'
import teacherService from '../../services/teacher.service'

const emptyForm = {
  academicPeriodId: '',
  courseId: '',
  teacherId: '',
  sectionCapacity: 30,
  scheduleBlocks: [],
}

const toScheduleBlock = (code) => ({ scheduleDay: code[0], module: Number(code.slice(1)) })

function AddEditSection() {
  const { id } = useParams()
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const [form, setForm] = useState({ ...emptyForm, academicPeriodId: searchParams.get('academicPeriodId') ?? '' })
  const [periods, setPeriods] = useState([])
  const [courses, setCourses] = useState([])
  const [teachers, setTeachers] = useState([])
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)
  const isEdit = Boolean(id)

  useEffect(() => {
    Promise.all([
      academicPeriodService.getAll(),
      courseService.getAll(),
      teacherService.getAll(),
      id ? sectionService.get(id) : null,
    ])
      .then(([periodsResponse, coursesResponse, teachersResponse, sectionResponse]) => {
        setPeriods(periodsResponse.data)
        setCourses(coursesResponse.data)
        setTeachers(teachersResponse.data)
        if (!sectionResponse) return
        const section = sectionResponse.data
        setForm({
          academicPeriodId: String(section.academicPeriodId),
          courseId: String(section.courseId),
          teacherId: String(section.teacherId),
          sectionCapacity: section.sectionCapacity,
          scheduleBlocks: section.scheduleBlocks,
        })
      })
      .catch((err) => setError(err.message))
  }, [id])

  const availablePeriods = periods.filter(
    (period) =>
      period.academicPeriodStatus === 'OPEN' || String(period.academicPeriodId) === form.academicPeriodId,
  )
  const availableTeachers = teachers.filter(
    (teacher) => teacher.teacherStatus === 'ACTIVE' || String(teacher.teacherId) === form.teacherId,
  )
  const selectedCourse = courses.find((course) => String(course.courseId) === form.courseId)
  const requiredBlocks = selectedCourse
    ? (selectedCourse.theoryHours + selectedCourse.exerciseHours + selectedCourse.labHours) / HOURS_PER_BLOCK
    : null
  const listUrl = form.academicPeriodId
    ? `/admin/sections?academicPeriodId=${form.academicPeriodId}`
    : '/admin/sections'

  const handleChange = (event) => {
    const { name, value } = event.target
    setForm({ ...form, [name]: value })
  }

  const toggleBlock = (code) => {
    const blocks = form.scheduleBlocks
    setForm({
      ...form,
      scheduleBlocks: blocks.includes(code) ? blocks.filter((item) => item !== code) : [...blocks, code],
    })
  }

  const handleSubmit = (event) => {
    event.preventDefault()
    setError('')
    setSaving(true)
    const data = {
      teacherId: Number(form.teacherId),
      sectionCapacity: Number(form.sectionCapacity),
      scheduleBlocks: form.scheduleBlocks.map(toScheduleBlock),
    }
    const request = isEdit
      ? sectionService.update(id, data)
      : sectionService.create({
          ...data,
          courseId: Number(form.courseId),
          academicPeriodId: Number(form.academicPeriodId),
        })
    request
      .then(() => navigate(listUrl))
      .catch((err) => {
        setError(err.message)
        setSaving(false)
      })
  }

  return (
    <>
      <PageHeader title={isEdit ? 'Edit section' : 'New section'} backTo={listUrl} />
      <ErrorAlert message={error} />
      <FormCard onSubmit={handleSubmit} cancelTo={listUrl} saving={saving}>
        <SectionFormFields
          form={form}
          periods={availablePeriods}
          courses={courses}
          teachers={availableTeachers}
          isEdit={isEdit}
          onChange={handleChange}
        />
        <Divider sx={{ my: 3 }} />
        <ScheduleGrid selected={form.scheduleBlocks} required={requiredBlocks} onToggle={toggleBlock} />
      </FormCard>
    </>
  )
}

export default AddEditSection
