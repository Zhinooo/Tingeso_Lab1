import Grid from '@mui/material/Grid'
import Paper from '@mui/material/Paper'
import TableCell from '@mui/material/TableCell'
import TableRow from '@mui/material/TableRow'
import DataTable from './DataTable'
import Stat from './Stat'
import StatusChip from './StatusChip'
import { formatPeriod } from '../utils/periods'

const COLUMNS = [
  { label: 'Período' },
  { label: 'Código' },
  { label: 'Ramo' },
  { label: 'Nota final', align: 'center' },
  { label: 'Resultado' },
]

function HistoryView({ student, history, loading }) {
  const graded = history.filter((enrollment) => enrollment.finalGrade != null)
  const average = graded.length
    ? (graded.reduce((sum, enrollment) => sum + enrollment.finalGrade, 0) / graded.length).toFixed(1)
    : '-'
  const approved = graded.filter((enrollment) => enrollment.result === 'APPROVED').length

  return (
    <>
      {student && (
        <Paper sx={{ p: 3, mb: 3 }}>
          <Grid container spacing={2}>
            <Grid size={4}>
              <Stat label="Estudiante" value={student.studentFullname} />
            </Grid>
            <Grid size={2}>
              <Stat label="RUN" value={student.studentRun} />
            </Grid>
            <Grid size={2}>
              <Stat label="Carrera" value={student.careerCode} />
            </Grid>
            <Grid size={2}>
              <Stat label="Promedio" value={average} />
            </Grid>
            <Grid size={2}>
              <Stat label="Aprobados" value={`${approved} / ${graded.length}`} />
            </Grid>
          </Grid>
        </Paper>
      )}
      <DataTable
        columns={COLUMNS}
        rowCount={history.length}
        loading={loading}
        emptyMessage="Aún no hay ramos con nota"
        emptyHint="Las notas finales aparecerán aquí cuando se cierren los períodos."
      >
        {history.map((enrollment) => (
          <TableRow key={enrollment.enrollmentId} hover>
            <TableCell>{formatPeriod(enrollment)}</TableCell>
            <TableCell sx={{ fontFamily: 'monospace', fontWeight: 700 }}>{enrollment.courseCode}</TableCell>
            <TableCell>{enrollment.courseName}</TableCell>
            <TableCell
              align="center"
              sx={{ fontWeight: 700, color: enrollment.result === 'APPROVED' ? 'success.main' : 'error.main' }}
            >
              {enrollment.finalGrade?.toFixed(1)}
            </TableCell>
            <TableCell>
              <StatusChip status={enrollment.result} />
            </TableCell>
          </TableRow>
        ))}
      </DataTable>
    </>
  )
}

export default HistoryView