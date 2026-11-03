import Grid from '@mui/material/Grid'
import { STATUS_LABEL } from '../../constants/statuses'
import { formatPeriod } from '../../utils/periods'
import SummaryCard from './SummaryCard'

function AdminSummary({ summary }) {
  const period = summary.currentPeriod
  const periodQuery = period ? `?academicPeriodId=${period.academicPeriodId}` : ''
  const statusDetail = summary.studentsByStatus
    .map((entry) => `${entry.count} ${STATUS_LABEL[entry.status].toLowerCase()}`)
    .join(' · ')

  return (
    <Grid container spacing={2}>
      <Grid size={3}>
        <SummaryCard
          label="Período abierto"
          value={period ? formatPeriod(period) : 'Ninguno'}
          detail={
            period
              ? summary.openPeriodCount > 1
                ? `Hay ${summary.openPeriodCount} períodos abiertos; se muestra el más reciente.`
                : 'Las cifras de la derecha corresponden a este período.'
              : 'Crea un período para empezar a ofrecer secciones.'
          }
          linkLabel="Ver períodos"
          to="/admin/academic-periods"
        />
      </Grid>
      <Grid size={3}>
        <SummaryCard
          label="Estudiantes"
          value={summary.studentCount}
          detail={statusDetail || 'Aún no hay estudiantes registrados.'}
          linkLabel="Ver estudiantes"
          to="/admin/students"
        />
      </Grid>
      <Grid size={3}>
        <SummaryCard
          label="Secciones del período"
          value={period ? summary.sectionCount : '-'}
          detail={period ? 'Ramos ofrecidos en el período abierto.' : 'Sin período abierto.'}
          linkLabel="Ver secciones"
          to={`/admin/sections${periodQuery}`}
        />
      </Grid>
      <Grid size={3}>
        <SummaryCard
          label="Inscripciones del período"
          value={period ? summary.enrollmentCount : '-'}
          detail={
            period
              ? summary.pendingGradeCount > 0
                ? `${summary.pendingGradeCount} sin nota final: el período aún no se puede cerrar.`
                : 'Todas tienen nota final.'
              : 'Sin período abierto.'
          }
          linkLabel="Ver inscripciones"
          to={`/admin/enrollments${periodQuery}`}
        />
      </Grid>
    </Grid>
  )
}

export default AdminSummary
