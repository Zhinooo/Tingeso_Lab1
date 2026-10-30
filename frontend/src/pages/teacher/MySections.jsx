import { useEffect, useState } from 'react'
import Grid from '@mui/material/Grid'
import LinearProgress from '@mui/material/LinearProgress'
import Paper from '@mui/material/Paper'
import EmptyState from '../../components/EmptyState'
import ErrorAlert from '../../components/ErrorAlert'
import PageHeader from '../../components/PageHeader'
import TeacherSectionCard from '../../components/teaching/TeacherSectionCard'
import academicPeriodService from '../../services/academicPeriod.service'
import sectionService from '../../services/section.service'
import { comparePeriodsDesc } from '../../utils/periods'

function MySections() {
  const [sections, setSections] = useState([])
  const [openPeriodIds, setOpenPeriodIds] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([sectionService.getMine(), academicPeriodService.getAll()])
      .then(([sectionsResponse, periodsResponse]) => {
        setSections([...sectionsResponse.data].sort(comparePeriodsDesc))
        setOpenPeriodIds(
          periodsResponse.data
            .filter((period) => period.academicPeriodStatus === 'OPEN')
            .map((period) => period.academicPeriodId),
        )
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  return (
    <>
      <PageHeader title="Mis secciones" subtitle="Secciones asignadas a ti. Abre una para registrar las notas finales." />
      <ErrorAlert message={error} />
      {loading && <LinearProgress sx={{ mb: 2 }} />}
      {!loading && sections.length === 0 && (
        <Paper>
          <EmptyState message="Aún no tienes secciones asignadas" />
        </Paper>
      )}
      <Grid container spacing={2}>
        {sections.map((section) => (
          <Grid key={section.sectionId} size={4}>
            <TeacherSectionCard section={section} isOpen={openPeriodIds.includes(section.academicPeriodId)} />
          </Grid>
        ))}
      </Grid>
    </>
  )
}

export default MySections
