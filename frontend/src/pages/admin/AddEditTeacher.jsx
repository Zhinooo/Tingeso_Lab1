import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import ErrorAlert from '../../components/ErrorAlert'
import FormCard from '../../components/FormCard'
import PageHeader from '../../components/PageHeader'
import TeacherFormFields from '../../components/teachers/TeacherFormFields'
import teacherService from '../../services/teacher.service'

const emptyForm = {
  teacherRun: '',
  teacherFullname: '',
  teacherEmail: '',
  teacherProtitle: '',
  teacherDegree: '',
  password: '',
}

function AddEditTeacher() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [form, setForm] = useState(emptyForm)
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)
  const isEdit = Boolean(id)

  useEffect(() => {
    if (!id) return
    teacherService
      .get(id)
      .then((response) => {
        const teacher = response.data
        setForm({
          teacherRun: teacher.teacherRun,
          teacherFullname: teacher.teacherFullname,
          teacherEmail: teacher.teacherEmail,
          teacherProtitle: teacher.teacherProtitle,
          teacherDegree: teacher.teacherDegree,
          password: '',
        })
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
    const request = isEdit
      ? teacherService.update(id, {
          teacherFullname: form.teacherFullname,
          teacherEmail: form.teacherEmail,
          teacherProtitle: form.teacherProtitle,
          teacherDegree: form.teacherDegree,
        })
      : teacherService.create(form)
    request
      .then(() => navigate('/admin/teachers'))
      .catch((err) => {
        setError(err.message)
        setSaving(false)
      })
  }

  return (
    <>
      <PageHeader title={isEdit ? 'Edit teacher' : 'New teacher'} backTo="/admin/teachers" />
      <ErrorAlert message={error} />
      <FormCard onSubmit={handleSubmit} cancelTo="/admin/teachers" saving={saving}>
        <TeacherFormFields form={form} isEdit={isEdit} onChange={handleChange} />
      </FormCard>
    </>
  )
}

export default AddEditTeacher
