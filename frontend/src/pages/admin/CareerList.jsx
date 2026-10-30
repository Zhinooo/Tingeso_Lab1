import { useEffect, useState } from 'react'
import ConfirmDialog from '../../components/ConfirmDialog'
import ErrorAlert from '../../components/ErrorAlert'
import NewButton from '../../components/NewButton'
import PageHeader from '../../components/PageHeader'
import CareerTable from '../../components/careers/CareerTable'
import careerService from '../../services/career.service'

function CareerList() {
  const [careers, setCareers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [toDelete, setToDelete] = useState(null)

  useEffect(() => {
    careerService
      .getAll()
      .then((response) => setCareers(response.data))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  const handleToggleStatus = (career) => {
    const newStatus = career.careerStatus === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
    setError('')
    careerService
      .changeStatus(career.careerId, newStatus)
      .then((response) =>
        setCareers((current) =>
          current.map((item) => (item.careerId === career.careerId ? response.data : item)),
        ),
      )
      .catch((err) => setError(err.message))
  }

  const handleDelete = () => {
    const career = toDelete
    setToDelete(null)
    setError('')
    careerService
      .remove(career.careerId)
      .then(() => setCareers((current) => current.filter((item) => item.careerId !== career.careerId)))
      .catch((err) => setError(err.message))
  }

  return (
    <>
      <PageHeader
        title="Careers"
        subtitle="Technical programs offered by the institute."
        action={<NewButton to="/admin/careers/new">New career</NewButton>}
      />
      <ErrorAlert message={error} onClose={() => setError('')} />
      <CareerTable
        careers={careers}
        loading={loading}
        onToggleStatus={handleToggleStatus}
        onDelete={setToDelete}
      />
      <ConfirmDialog
        open={Boolean(toDelete)}
        title="Delete career?"
        message={toDelete && `"${toDelete.careerName}" will be permanently deleted.`}
        onConfirm={handleDelete}
        onClose={() => setToDelete(null)}
      />
    </>
  )
}

export default CareerList