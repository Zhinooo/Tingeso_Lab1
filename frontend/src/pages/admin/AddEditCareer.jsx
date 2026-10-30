import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import ErrorAlert from '../../components/ErrorAlert'
import FormCard from '../../components/FormCard'
import PageHeader from '../../components/PageHeader'
import CareerFormFields from '../../components/careers/CareerFormFields'
import careerService from '../../services/career.service'

const emptyForm = { careerCode: '', careerName: '', careerDescription: '' }

function AddEditCareer() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [form, setForm] = useState(emptyForm)
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)
  const isEdit = Boolean(id)

  useEffect(() => {
    if (!id) return
    careerService
      .get(id)
      .then((response) => {
        const { careerCode, careerName, careerDescription } = response.data
        setForm({ careerCode, careerName, careerDescription })
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
    const request = isEdit ? careerService.update(id, form) : careerService.create(form)
    request
      .then(() => navigate('/admin/careers'))
      .catch((err) => {
        setError(err.message)
        setSaving(false)
      })
  }

  return (
    <>
      <PageHeader title={isEdit ? 'Editar carrera' : 'Nueva carrera'} backTo="/admin/careers" />
      <ErrorAlert message={error} />
      <FormCard onSubmit={handleSubmit} cancelTo="/admin/careers" saving={saving}>
        <CareerFormFields form={form} onChange={handleChange} />
      </FormCard>
    </>
  )
}

export default AddEditCareer