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
        title="Períodos académicos"
        subtitle="Semestres en los que se ofrecen secciones."
        action={<NewButton to="/admin/academic-periods/new">Nuevo período</NewButton>}
      />
      <ErrorAlert title="No se puede cerrar el período" message={closeError} onClose={() => setCloseError('')} />
      <ErrorAlert message={error} onClose={() => setError('')} />
      <PeriodTable
        periods={periods}
        loading={loading}
        onClosePeriod={setToClose}
        onDelete={setToDelete}
      />
      <ConfirmDialog
        open={Boolean(toClose)}
        title={toClose && `¿Cerrar el período ${formatPeriod(toClose)}?`}
        message="Esta acción es irreversible. Todas las inscripciones deben tener nota final y se actualizará el estado de los estudiantes."
        confirmLabel="Cerrar período"
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
