import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import Box from '@mui/material/Box'
import ConfirmDialog from '../../components/ConfirmDialog'
import ErrorAlert from '../../components/ErrorAlert'
import NewButton from '../../components/NewButton'
import PageHeader from '../../components/PageHeader'
import PeriodFilter from '../../components/PeriodFilter'
import SectionTable from '../../components/sections/SectionTable'
import academicPeriodService from '../../services/academicPeriod.service'
import sectionService from '../../services/section.service'
import { formatPeriod } from '../../utils/periods'

function SectionList() {
  const [searchParams, setSearchParams] = useSearchParams()
  const academicPeriodId = searchParams.get('academicPeriodId') ?? ''
  const [periods, setPeriods] = useState([])
  const [sections, setSections] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [toDelete, setToDelete] = useState(null)

  useEffect(() => {
    academicPeriodService
      .getAll()
      .then((response) => setPeriods(response.data))
      .catch((err) => setError(err.message))
  }, [])

  useEffect(() => {
    const request = academicPeriodId
      ? sectionService.getByAcademicPeriod(academicPeriodId)
      : sectionService.getAll()
    request
      .then((response) => setSections(response.data))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [academicPeriodId])

  const handlePeriodChange = (value) => {
    setLoading(true)
    setSearchParams(value ? { academicPeriodId: value } : {})
  }

  const handleDelete = () => {
    const section = toDelete
    setToDelete(null)
    setError('')
    sectionService
      .remove(section.sectionId)
      .then(() => setSections((current) => current.filter((item) => item.sectionId !== section.sectionId)))
      .catch((err) => setError(err.message))
  }

  const newSectionUrl = academicPeriodId
    ? `/admin/sections/new?academicPeriodId=${academicPeriodId}`
    : '/admin/sections/new'

  return (
    <>
      <PageHeader
        title="Secciones"
        subtitle="Ramos dictados en cada período, con su docente y horario semanal."
        action={<NewButton to={newSectionUrl}>Nueva sección</NewButton>}
      />
      <Box sx={{ mb: 2 }}>
        <PeriodFilter periods={periods} value={academicPeriodId} onChange={handlePeriodChange} />
      </Box>
      <ErrorAlert message={error} onClose={() => setError('')} />
      <SectionTable
        sections={sections}
        loading={loading}
        emptyHint={academicPeriodId ? 'Este período aún no tiene secciones.' : 'Primero crea un período abierto y luego agrega secciones.'}
        onDelete={setToDelete}
      />
      <ConfirmDialog
        open={Boolean(toDelete)}
        title="¿Eliminar sección?"
        message={toDelete && `La sección ${toDelete.courseCode} (${formatPeriod(toDelete)}) se eliminará de forma permanente.`}
        onConfirm={handleDelete}
        onClose={() => setToDelete(null)}
      />
    </>
  )
}

export default SectionList
