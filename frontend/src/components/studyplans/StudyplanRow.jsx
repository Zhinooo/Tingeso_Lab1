import TableCell from '@mui/material/TableCell'
import TableRow from '@mui/material/TableRow'
import RowAction from '../RowAction'
import RowActions from '../RowActions'
import StatusChip from '../StatusChip'

function StudyplanRow({ studyplan, onToggleStatus, onDelete }) {
  const isCurrent = studyplan.studyplanStatus === 'CURRENT'

  return (
    <TableRow hover>
      <TableCell sx={{ fontWeight: 700, fontFamily: 'monospace' }}>{studyplan.studyplanIdentifier}</TableCell>
      <TableCell>{studyplan.careerCode}</TableCell>
      <TableCell>
        <StatusChip status={studyplan.studyplanStatus} />
      </TableCell>
      <TableCell align="right">
        <RowActions>
          <RowAction
            label="Ramos"
            color="primary"
            to={`/admin/courses?studyplanId=${studyplan.studyplanId}`}
          />
          <RowAction label="Editar" to={`/admin/studyplans/${studyplan.studyplanId}/edit`} />
          <RowAction
            label={isCurrent ? 'Quitar vigente' : 'Dejar vigente'}
            color={isCurrent ? 'inherit' : 'warning'}
            onClick={() => onToggleStatus(studyplan)}
          />
          <RowAction label="Eliminar" color="error" onClick={() => onDelete(studyplan)} />
        </RowActions>
      </TableCell>
    </TableRow>
  )
}

export default StudyplanRow