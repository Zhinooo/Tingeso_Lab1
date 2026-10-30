import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import ErrorAlert from '../../components/ErrorAlert'
import FormCard from '../../components/FormCard'
import PageHeader from '../../components/PageHeader'
import PeriodFormFields from '../../components/periods/PeriodFormFields'
import academicPeriodService from '../../services/academicPeriod.service'
import { formatPeriod } from '../../utils/periods'

const emptyForm = { academicYear: new Date().getFullYear(), academicSemester: 1 }

function AddEditAcademicPeriod() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [form, setForm] = useState(emptyForm)
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)
  const isEdit = Boolean(id)

  useEffect(() => {
    if (!id) return
    academicPeriodService
      .get(id)
      .then((response) => {
        const { academicYear, academicSemester } = response.data
        setForm({ academicYear, academicSemester })
      })
      .catch((err) => setError(err.message))
  }, [id])

  const handleChange = (event) => {
    const { name, value } = event.target
    setForm({ ...form, [name]: value })
  }

  const handleSubmit = (event) => {
    event.preventDefault()
    setError('')
    setSaving(true)
    const data = {
      academicYear: Number(form.academicYear),
      academicSemester: Number(form.academicSemester),
    }
    const request = isEdit ? academicPeriodService.update(id, data) : academicPeriodService.create(data)
    request
      .then(() => navigate('/admin/academic-periods'))
      .catch((err) => {
        setError(err.message)
        setSaving(false)
      })
  }

  return (
    <>
      <PageHeader
        title={isEdit ? 'Edit academic period' : 'New academic period'}
        backTo="/admin/academic-periods"
      />
      <ErrorAlert message={error} />
      <FormCard
        description={`Period: ${formatPeriod(form)}`}
        onSubmit={handleSubmit}
        cancelTo="/admin/academic-periods"
        saving={saving}
      >
        <PeriodFormFields form={form} onChange={handleChange} />
      </FormCard>
    </>
  )
}

export default AddEditAcademicPeriod
