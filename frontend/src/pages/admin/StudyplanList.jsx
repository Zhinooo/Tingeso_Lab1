import { useEffect, useState } from 'react'
import ConfirmDialog from '../../components/ConfirmDialog'
import ErrorAlert from '../../components/ErrorAlert'
import NewButton from '../../components/NewButton'
import PageHeader from '../../components/PageHeader'
import StudyplanTable from '../../components/studyplans/StudyplanTable'
import studyplanService from '../../services/studyplan.service'

function StudyplanList() {
  const [studyplans, setStudyplans] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [toDelete, setToDelete] = useState(null)

  useEffect(() => {
    studyplanService
      .getAll()
      .then((response) => setStudyplans(response.data))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  const handleToggleStatus = (studyplan) => {
    const newStatus = studyplan.studyplanStatus === 'CURRENT' ? 'NOT_CURRENT' : 'CURRENT'
    setError('')
    studyplanService
      .changeStatus(studyplan.studyplanId, newStatus)
      .then(() => studyplanService.getAll())
      .then((response) => setStudyplans(response.data))
      .catch((err) => setError(err.message))
  }

  const handleDelete = () => {
    const studyplan = toDelete
    setToDelete(null)
    setError('')
    studyplanService
      .remove(studyplan.studyplanId)
      .then(() =>
        setStudyplans((current) => current.filter((item) => item.studyplanId !== studyplan.studyplanId)),
      )
      .catch((err) => setError(err.message))
  }

  return (
    <>
      <PageHeader
        title="Planes de estudio"
        subtitle="Cada carrera puede tener varios planes; solo uno está vigente."
        action={<NewButton to="/admin/studyplans/new">Nuevo plan de estudio</NewButton>}
      />
      <ErrorAlert message={error} onClose={() => setError('')} />
      <StudyplanTable
        studyplans={studyplans}
        loading={loading}
        onToggleStatus={handleToggleStatus}
        onDelete={setToDelete}
      />
      <ConfirmDialog
        open={Boolean(toDelete)}
        title="¿Eliminar plan de estudio?"
        message={toDelete && `"${toDelete.studyplanIdentifier}" se eliminará de forma permanente.`}
        onConfirm={handleDelete}
        onClose={() => setToDelete(null)}
      />
    </>
  )
}

export default StudyplanList