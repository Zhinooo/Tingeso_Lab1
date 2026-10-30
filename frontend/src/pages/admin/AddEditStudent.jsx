import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import Snackbar from '@mui/material/Snackbar'
import ErrorAlert from '../../components/ErrorAlert'
import FormCard from '../../components/FormCard'
import PageHeader from '../../components/PageHeader'
import StudentCareerChangeCard from '../../components/students/StudentCareerChangeCard'
import StudentFormFields from '../../components/students/StudentFormFields'
import careerService from '../../services/career.service'
import studentService from '../../services/student.service'
import studyplanService from '../../services/studyplan.service'

const emptyForm = {
  studentRun: '',
  studentFullname: '',
  studentEmail: '',
  careerCode: '',
  password: '',
}

const emptyCareerForm = { careerCode: '', studyplanIdentifier: '' }

function AddEditStudent() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [form, setForm] = useState(emptyForm)
  const [careerForm, setCareerForm] = useState(emptyCareerForm)
  const [careers, setCareers] = useState([])
  const [studyplans, setStudyplans] = useState([])
  const [error, setError] = useState('')
  const [careerError, setCareerError] = useState('')
  const [saving, setSaving] = useState(false)
  const [savingCareer, setSavingCareer] = useState(false)
  const [careerChanged, setCareerChanged] = useState(false)
  const isEdit = Boolean(id)

  useEffect(() => {
    Promise.all([careerService.getAll(), studyplanService.getAll(), id ? studentService.get(id) : null])
      .then(([careersResponse, studyplansResponse, studentResponse]) => {
        setCareers(careersResponse.data)
        setStudyplans(studyplansResponse.data)
        if (!studentResponse) return
        const student = studentResponse.data
        setForm({
          studentRun: student.studentRun,
          studentFullname: student.studentFullname,
          studentEmail: student.studentEmail,
          careerCode: student.careerCode,
          password: '',
        })
        setCareerForm({ careerCode: student.careerCode, studyplanIdentifier: student.studyplanIdentifier })
      })
      .catch((err) => setError(err.message))
  }, [id])

  const activeCareers = careers.filter((career) => career.careerStatus === 'ACTIVE')

  const handleChange = (event) => {
    const { name, value } = event.target
    setForm({ ...form, [name]: value })
  }

  const handleCareerChange = (event) => {
    const { name, value } = event.target
    if (name === 'careerCode') {
      setCareerForm({ careerCode: value, studyplanIdentifier: '' })
    } else {
      setCareerForm({ ...careerForm, [name]: value })
    }
  }

  const handleSubmit = (event) => {
    event.preventDefault()
    setError('')
    setSaving(true)
    const request = isEdit
      ? studentService.update(id, { studentFullname: form.studentFullname, studentEmail: form.studentEmail })
      : studentService.create(form)
    request
      .then(() => navigate('/admin/students'))
      .catch((err) => {
        setError(err.message)
        setSaving(false)
      })
  }

  const handleCareerSubmit = (event) => {
    event.preventDefault()
    setCareerError('')
    setSavingCareer(true)
    studentService
      .changeCareer(id, careerForm)
      .then((response) => {
        setCareerForm({
          careerCode: response.data.careerCode,
          studyplanIdentifier: response.data.studyplanIdentifier,
        })
        setCareerChanged(true)
      })
      .catch((err) => setCareerError(err.message))
      .finally(() => setSavingCareer(false))
  }

  return (
    <>
      <PageHeader title={isEdit ? 'Editar estudiante' : 'Nuevo estudiante'} backTo="/admin/students" />
      <ErrorAlert message={error} />
      <FormCard
        title={isEdit ? 'Datos personales' : undefined}
        onSubmit={handleSubmit}
        cancelTo="/admin/students"
        saving={saving}
      >
        <StudentFormFields form={form} careers={activeCareers} isEdit={isEdit} onChange={handleChange} />
      </FormCard>
      {isEdit && (
        <StudentCareerChangeCard
          careerForm={careerForm}
          careers={activeCareers}
          studyplans={studyplans}
          error={careerError}
          saving={savingCareer}
          onChange={handleCareerChange}
          onSubmit={handleCareerSubmit}
        />
      )}
      <Snackbar
        open={careerChanged}
        autoHideDuration={3000}
        onClose={() => setCareerChanged(false)}
        message="Carrera y plan de estudio actualizados"
      />
    </>
  )
}

export default AddEditStudent
