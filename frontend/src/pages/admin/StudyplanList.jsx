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
        title="Study plans"
        subtitle="Each career can have several plans; only one is current."
        action={<NewButton to="/admin/studyplans/new">New study plan</NewButton>}
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
        title="Delete study plan?"
        message={toDelete && `"${toDelete.studyplanIdentifier}" will be permanently deleted.`}
        onConfirm={handleDelete}
        onClose={() => setToDelete(null)}
      />
    </>
  )
}

export default StudyplanList