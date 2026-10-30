import TableCell from '@mui/material/TableCell'
import TableRow from '@mui/material/TableRow'
import { formatPeriod } from '../../utils/periods'
import RowAction from '../RowAction'
import RowActions from '../RowActions'
import StatusChip from '../StatusChip'

function PeriodRow({ period, onClosePeriod, onDelete }) {
  const isOpen = period.academicPeriodStatus === 'OPEN'

  return (
    <TableRow hover>
      <TableCell sx={{ fontWeight: 700, fontSize: 15 }}>{formatPeriod(period)}</TableCell>
      <TableCell>
        <StatusChip status={period.academicPeriodStatus} />
      </TableCell>
      <TableCell align="right">
        <RowActions>
          <RowAction
            label="Secciones"
            color="primary"
            to={`/admin/sections?academicPeriodId=${period.academicPeriodId}`}
          />
          {isOpen && (
            <>
              <RowAction label="Editar" to={`/admin/academic-periods/${period.academicPeriodId}/edit`} />
              <RowAction label="Cerrar período" color="warning" onClick={() => onClosePeriod(period)} />
              <RowAction label="Eliminar" color="error" onClick={() => onDelete(period)} />
            </>
          )}
        </RowActions>
      </TableCell>
    </TableRow>
  )
}

export default PeriodRow
