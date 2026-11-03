import { useEffect, useState } from 'react'
import ConfirmDialog from '../../components/ConfirmDialog'
import ErrorAlert from '../../components/ErrorAlert'
import NewButton from '../../components/NewButton'
import PageHeader from '../../components/PageHeader'
import ClosePeriodDialog from '../../components/periods/ClosePeriodDialog'
import PeriodTable from '../../components/periods/PeriodTable'
import academicPeriodService from '../../services/academicPeriod.service'
import enrollmentService from '../../services/enrollment.service'
import { summarizePendingGrades } from '../../utils/grades'
import { formatPeriod } from '../../utils/periods'

function AcademicPeriodList() {
  const [periods, setPeriods] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [closeError, setCloseError] = useState('')
  const [toClose, setToClose] = useState(null)
  const [closeSummary, setCloseSummary] = useState(null)
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

  const handleReviewClose = (period) => {
    setToClose(period)
    setCloseSummary(null)
    setCloseError('')
    enrollmentService
      .getByAcademicPeriod(period.academicPeriodId)
      .then((response) => setCloseSummary(summarizePendingGrades(response.data)))
      .catch((err) => {
        setToClose(null)
        setCloseError(err.message)
      })
  }

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
        title="Períodos académicos"
        subtitle="Semestres en los que se ofrecen secciones."
        action={<NewButton to="/admin/academic-periods/new">Nuevo período</NewButton>}
      />
      <ErrorAlert title="No se puede cerrar el período" message={closeError} onClose={() => setCloseError('')} />
      <ErrorAlert message={error} onClose={() => setError('')} />
      <PeriodTable
        periods={periods}
        loading={loading}
        onClosePeriod={handleReviewClose}
        onDelete={setToDelete}
      />
      <ClosePeriodDialog
        period={toClose}
        summary={closeSummary}
        onConfirm={handleClosePeriod}
        onClose={() => setToClose(null)}
      />
      <ConfirmDialog
        open={Boolean(toDelete)}
        title="¿Eliminar período?"
        message={toDelete && `El período ${formatPeriod(toDelete)} se eliminará de forma permanente.`}
        onConfirm={handleDelete}
        onClose={() => setToDelete(null)}
      />
    </>
  )
}

export default AcademicPeriodList
