import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import ErrorAlert from '../../components/ErrorAlert'
import FormCard from '../../components/FormCard'
import PageHeader from '../../components/PageHeader'
import TuitionFormFields from '../../components/tuitions/TuitionFormFields'
import { FINAL_STUDENT_STATUSES } from '../../constants/students'
import academicPeriodService from '../../services/academicPeriod.service'
import studentService from '../../services/student.service'
import tuitionService from '../../services/tuition.service'

function AddTuition() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const [form, setForm] = useState({ studentId: '', academicPeriodId: searchParams.get('academicPeriodId') ?? '' })
  const [students, setStudents] = useState([])
  const [periods, setPeriods] = useState([])
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    Promise.all([studentService.getAll(), academicPeriodService.getAll()])
      .then(([studentsResponse, periodsResponse]) => {
        setStudents(studentsResponse.data)
        setPeriods(periodsResponse.data)
      })
      .catch((err) => setError(err.message))
  }, [])

  const availableStudents = students.filter((student) => !FINAL_STUDENT_STATUSES.includes(student.studentStatus))
  const openPeriods = periods.filter((period) => period.academicPeriodStatus === 'OPEN')
  const isOpenPeriodSelected = openPeriods.some((period) => String(period.academicPeriodId) === form.academicPeriodId)
  const listUrl = form.academicPeriodId
    ? `/admin/tuitions?academicPeriodId=${form.academicPeriodId}`
    : '/admin/tuitions'

  const handleChange = (event) => {
    const { name, value } = event.target
    setForm({ ...form, [name]: value })
  }

  const handleSubmit = (event) => {
    event.preventDefault()
    setError('')
    setSaving(true)
    tuitionService
      .create({ studentId: Number(form.studentId), academicPeriodId: Number(form.academicPeriodId) })
      .then(() => navigate(listUrl))
      .catch((err) => {
        setError(err.message)
        setSaving(false)
      })
  }

  return (
    <>
      <PageHeader title="New tuition" backTo={listUrl} />
      <ErrorAlert message={error} />
      <FormCard onSubmit={handleSubmit} cancelTo={listUrl} saving={saving}>
        <TuitionFormFields
          form={{ ...form, academicPeriodId: isOpenPeriodSelected ? form.academicPeriodId : '' }}
          students={availableStudents}
          periods={openPeriods}
          onChange={handleChange}
        />
      </FormCard>
    </>
  )
}

export default AddTuition
