import Grid from '@mui/material/Grid'
import Paper from '@mui/material/Paper'
import TableCell from '@mui/material/TableCell'
import TableRow from '@mui/material/TableRow'
import DataTable from './DataTable'
import Stat from './Stat'
import StatusChip from './StatusChip'

const COLUMNS = [
  { label: 'Period' },
  { label: 'Code' },
  { label: 'Course' },
  { label: 'Final grade', align: 'center' },
  { label: 'Result' },
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
              <Stat label="Student" value={student.studentFullname} />
            </Grid>
            <Grid size={2}>
              <Stat label="RUN" value={student.studentRun} />
            </Grid>
            <Grid size={2}>
              <Stat label="Career" value={student.careerCode} />
            </Grid>
            <Grid size={2}>
              <Stat label="Average" value={average} />
            </Grid>
            <Grid size={2}>
              <Stat label="Approved" value={`${approved} / ${graded.length}`} />
            </Grid>
          </Grid>
        </Paper>
      )}
      <DataTable
        columns={COLUMNS}
        rowCount={history.length}
        loading={loading}
        emptyMessage="No graded courses yet"
        emptyHint="Final grades will appear here once periods are closed."
      >
        {history.map((enrollment) => (
          <TableRow key={enrollment.enrollmentId} hover>
            <TableCell>{`${enrollment.academicYear}-${enrollment.academicSemester}`}</TableCell>
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