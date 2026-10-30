import TableCell from '@mui/material/TableCell'
import TableRow from '@mui/material/TableRow'
import EventBusyOutlinedIcon from '@mui/icons-material/EventBusyOutlined'
import { formatPeriod } from '../../utils/periods'
import RowAction from '../RowAction'
import StatusChip from '../StatusChip'

function MyEnrollmentRow({ enrollment, onCancel }) {
  const isGraded = enrollment.finalGrade != null

  return (
    <TableRow hover>
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
      <TableCell align="right">
        <RowAction
          title={isGraded ? 'Graded enrollments cannot be cancelled' : 'Cancel enrollment'}
          icon={<EventBusyOutlinedIcon fontSize="small" />}
          color="error"
          disabled={isGraded}
          onClick={() => onCancel(enrollment)}
        />
      </TableCell>
    </TableRow>
  )
}

export default MyEnrollmentRow
