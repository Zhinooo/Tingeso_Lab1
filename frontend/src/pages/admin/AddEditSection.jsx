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
  const [periodSections, setPeriodSections] = useState([])
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

  useEffect(() => {
    if (!form.academicPeriodId) return
    sectionService
      .getByAcademicPeriod(form.academicPeriodId)
      .then((response) => setPeriodSections(response.data))
      .catch((err) => setError(err.message))
  }, [form.academicPeriodId])

  const selectedCourse = courses.find((course) => String(course.courseId) === form.courseId)

  const isSameSemester = (section) => {
    const course = courses.find((item) => item.courseId === section.courseId)
    return (
      Boolean(selectedCourse && course) &&
      course.studyplanId === selectedCourse.studyplanId &&
      course.semester === selectedCourse.semester
    )
  }

  const isTaken = (section) => String(section.teacherId) === form.teacherId || isSameSemester(section)

  const otherSections = periodSections.filter(
    (section) => String(section.academicPeriodId) === form.academicPeriodId && section.sectionId !== Number(id),
  )

  const occupiedBlocks = {}
  otherSections
    .filter(isTaken)
    .forEach((section) => {
      section.scheduleBlocks.forEach((block) => {
        occupiedBlocks[block] = section.courseCode
      })
    })

  const availableCourses = isEdit
    ? courses
    : courses.filter((course) => !otherSections.some((section) => section.courseId === course.courseId))

  const availablePeriods = periods.filter(
    (period) =>
      period.academicPeriodStatus === 'OPEN' || String(period.academicPeriodId) === form.academicPeriodId,
  )
  const availableTeachers = teachers.filter(
    (teacher) => teacher.teacherStatus === 'ACTIVE' || String(teacher.teacherId) === form.teacherId,
  )
  const requiredBlocks = selectedCourse
    ? Math.ceil((selectedCourse.theoryHours + selectedCourse.exerciseHours + selectedCourse.labHours) / HOURS_PER_BLOCK)
    : null
  const listUrl = form.academicPeriodId
    ? `/admin/sections?academicPeriodId=${form.academicPeriodId}`
    : '/admin/sections'

  const handleChange = (event) => {
    const { name, value } = event.target
    const next = { ...form, [name]: value }
    if (name === 'academicPeriodId') {
      next.courseId = ''
    }
    setForm(next)
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
      <PageHeader title={isEdit ? 'Editar sección' : 'Nueva sección'} backTo={listUrl} />
      <ErrorAlert message={error} />
      <FormCard onSubmit={handleSubmit} cancelTo={listUrl} saving={saving}>
        <SectionFormFields
          form={form}
          periods={availablePeriods}
          courses={availableCourses}
          teachers={availableTeachers}
          isEdit={isEdit}
          onChange={handleChange}
        />
        <Divider sx={{ my: 3 }} />
        <ScheduleGrid
          selected={form.scheduleBlocks}
          occupied={occupiedBlocks}
          required={requiredBlocks}
          onToggle={toggleBlock}
        />
      </FormCard>
    </>
  )
}

export default AddEditSection
