import TableCell from '@mui/material/TableCell'
import TableRow from '@mui/material/TableRow'
import { formatPeriod } from '../../utils/periods'
import StatusChip from '../StatusChip'

function EnrollmentRow({ enrollment }) {
  return (
    <TableRow hover>
      <TableCell sx={{ fontFamily: 'monospace', whiteSpace: 'nowrap' }}>{enrollment.studentRun}</TableCell>
      <TableCell>{formatPeriod(enrollment)}</TableCell>
      <TableCell sx={{ fontFamily: 'monospace', fontWeight: 700 }}>{enrollment.courseCode}</TableCell>
      <TableCell>{enrollment.courseName}</TableCell>
      <TableCell>{enrollment.enrollmentDate}</TableCell>
      <TableCell align="center" sx={{ fontWeight: 700 }}>
        {enrollment.finalGrade?.toFixed(1) ?? '-'}
      </TableCell>
      <TableCell>
        <StatusChip status={enrollment.result} />
      </TableCell>
    </TableRow>
  )
}

export default EnrollmentRow
