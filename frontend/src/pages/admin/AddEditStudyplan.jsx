import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import ErrorAlert from '../../components/ErrorAlert'
import FormCard from '../../components/FormCard'
import PageHeader from '../../components/PageHeader'
import StudyplanFormFields from '../../components/studyplans/StudyplanFormFields'
import careerService from '../../services/career.service'
import studyplanService from '../../services/studyplan.service'

const emptyForm = { studyplanIdentifier: '', careerCode: '', current: true }

function AddEditStudyplan() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [form, setForm] = useState(emptyForm)
  const [careers, setCareers] = useState([])
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)
  const isEdit = Boolean(id)

  useEffect(() => {
    Promise.all([careerService.getAll(), id ? studyplanService.get(id) : null])
      .then(([careersResponse, studyplanResponse]) => {
        setCareers(careersResponse.data)
        if (!studyplanResponse) return
        const { studyplanIdentifier, careerCode, studyplanStatus } = studyplanResponse.data
        setForm({ studyplanIdentifier, careerCode, current: studyplanStatus === 'CURRENT' })
      })
      .catch((err) => setError(err.message))
  }, [id])

  const handleChange = (event) => {
    const { name, value, type, checked } = event.target
    setForm({ ...form, [name]: type === 'checkbox' ? checked : value })
  }

  const handleSubmit = (event) => {
    event.preventDefault()
    setError('')
    setSaving(true)
    const request = isEdit ? studyplanService.update(id, form) : studyplanService.create(form)
    request
      .then(() => navigate('/admin/studyplans'))
      .catch((err) => {
        setError(err.message)
        setSaving(false)
      })
  }

  return (
    <>
      <PageHeader title={isEdit ? 'Editar plan de estudio' : 'Nuevo plan de estudio'} backTo="/admin/studyplans" />
      <ErrorAlert message={error} />
      <FormCard onSubmit={handleSubmit} cancelTo="/admin/studyplans" saving={saving}>
        <StudyplanFormFields form={form} careers={careers} isEdit={isEdit} onChange={handleChange} />
      </FormCard>
    </>
  )
}

export default AddEditStudyplan