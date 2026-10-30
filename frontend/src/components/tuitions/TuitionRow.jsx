import TableCell from '@mui/material/TableCell'
import TableRow from '@mui/material/TableRow'
import { formatPeriod } from '../../utils/periods'
import RowAction from '../RowAction'

function TuitionRow({ tuition, studentName, onDelete }) {
  return (
    <TableRow hover>
      <TableCell sx={{ fontFamily: 'monospace', whiteSpace: 'nowrap' }}>{tuition.studentRun}</TableCell>
      <TableCell>{studentName}</TableCell>
      <TableCell>{formatPeriod(tuition)}</TableCell>
      <TableCell>{tuition.tuitionDate}</TableCell>
      <TableCell align="right">
        <RowAction label="Eliminar" color="error" onClick={() => onDelete(tuition)} />
      </TableCell>
    </TableRow>
  )
}

export default TuitionRow
