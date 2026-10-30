import { useEffect, useState } from 'react'
import ConfirmDialog from '../../components/ConfirmDialog'
import ErrorAlert from '../../components/ErrorAlert'
import NewButton from '../../components/NewButton'
import PageHeader from '../../components/PageHeader'
import PeriodTable from '../../components/periods/PeriodTable'
import academicPeriodService from '../../services/academicPeriod.service'
import { formatPeriod } from '../../utils/periods'

function AcademicPeriodList() {
  const [periods, setPeriods] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [closeError, setCloseError] = useState('')
  const [toClose, setToClose] = useState(null)
  const [toDelete, setToDelete] = useState(null)

  useEffect(() => {
    academicPeriodService
      .getAll()
      .then((response) => setPeriods(response.data))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  const replacePeriod = (updated) =>
    setPeriods((current) =>
      current.map((item) => (item.academicPeriodId === updated.academicPeriodId ? updated : item)),
    )

  const handleClosePeriod = () => {
    const period = toClose
    setToClose(null)
    setCloseError('')
    academicPeriodService
      .close(period.academicPeriodId)
      .then((response) => replacePeriod(response.data))
      .catch((err) => setCloseError(err.message))
  }

  const handleDelete = () => {
    const period = toDelete
    setToDelete(null)
    setError('')
    academicPeriodService
      .remove(period.academicPeriodId)
      .then(() =>
        setPeriods((current) => current.filter((item) => item.academicPeriodId !== period.academicPeriodId)),
      )
      .catch((err) => setError(err.message))
  }

  return (
    <>
      <PageHeader
        title="Academic periods"
        subtitle="Semesters in which sections are offered."
        action={<NewButton to="/admin/academic-periods/new">New period</NewButton>}
      />
      <ErrorAlert title="Cannot close period" message={closeError} onClose={() => setCloseError('')} />
      <ErrorAlert message={error} onClose={() => setError('')} />
      <PeriodTable
        periods={periods}
        loading={loading}
        onClosePeriod={setToClose}
        onDelete={setToDelete}
      />
      <ConfirmDialog
        open={Boolean(toClose)}
        title={toClose && `Close period ${formatPeriod(toClose)}?`}
        message="This action is irreversible. Every enrollment must have its final grade, and student statuses will be updated."
        confirmLabel="Close period"
        onConfirm={handleClosePeriod}
        onClose={() => setToClose(null)}
      />
      <ConfirmDialog
        open={Boolean(toDelete)}
        title="Delete period?"
        message={toDelete && `Period ${formatPeriod(toDelete)} will be permanently deleted.`}
        onConfirm={handleDelete}
        onClose={() => setToDelete(null)}
      />
    </>
  )
}

export default AcademicPeriodList
