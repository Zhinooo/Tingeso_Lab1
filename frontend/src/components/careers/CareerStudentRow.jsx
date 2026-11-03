import TableCell from '@mui/material/TableCell'
import TableRow from '@mui/material/TableRow'
import Typography from '@mui/material/Typography'
import StatusChip from '../StatusChip'

function CareerStudentRow({ student }) {
  return (
    <TableRow hover>
      <TableCell sx={{ fontFamily: 'monospace', whiteSpace: 'nowrap' }}>{student.studentRun}</TableCell>
      <TableCell sx={{ fontWeight: 500 }}>{student.studentFullname}</TableCell>
      <TableCell>
        <Typography variant="body2" color="text.secondary">
          {student.studentEmail}
        </Typography>
      </TableCell>
      <TableCell>{student.studyplanIdentifier}</TableCell>
      <TableCell>
        <StatusChip status={student.studentStatus} />
      </TableCell>
    </TableRow>
  )
}

export default CareerStudentRow
