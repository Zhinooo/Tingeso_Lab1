import TableCell from '@mui/material/TableCell'
import TableRow from '@mui/material/TableRow'
import Typography from '@mui/material/Typography'
import RowAction from '../RowAction'
import RowActions from '../RowActions'
import StatusChip from '../StatusChip'
import StatusToggleAction from '../StatusToggleAction'

function CareerRow({ career, onToggleStatus, onDelete }) {
  return (
    <TableRow hover>
      <TableCell sx={{ fontWeight: 700, fontFamily: 'monospace' }}>{career.careerCode}</TableCell>
      <TableCell sx={{ fontWeight: 500 }}>{career.careerName}</TableCell>
      <TableCell>
        <Typography variant="body2" color="text.secondary" sx={{ maxWidth: 360 }} noWrap>
          {career.careerDescription}
        </Typography>
      </TableCell>
      <TableCell>
        <StatusChip status={career.careerStatus} />
      </TableCell>
      <TableCell align="right">
        <RowActions>
          <RowAction label="Editar" to={`/admin/careers/${career.careerId}/edit`} />
          <StatusToggleAction active={career.careerStatus === 'ACTIVE'} onClick={() => onToggleStatus(career)} />
          <RowAction label="Eliminar" color="error" onClick={() => onDelete(career)} />
        </RowActions>
      </TableCell>
    </TableRow>
  )
}

export default CareerRow