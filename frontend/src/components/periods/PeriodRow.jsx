import TableCell from '@mui/material/TableCell'
import TableRow from '@mui/material/TableRow'
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutlineOutlined'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import LockOutlinedIcon from '@mui/icons-material/LockOutlined'
import ViewWeekOutlinedIcon from '@mui/icons-material/ViewWeekOutlined'
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
            title="Sections"
            icon={<ViewWeekOutlinedIcon fontSize="small" />}
            color="primary"
            to={`/admin/sections?academicPeriodId=${period.academicPeriodId}`}
          />
          <RowAction
            title="Edit"
            icon={<EditOutlinedIcon fontSize="small" />}
            to={`/admin/academic-periods/${period.academicPeriodId}/edit`}
          />
          {isOpen && (
            <RowAction
              title="Close period"
              icon={<LockOutlinedIcon fontSize="small" />}
              color="warning"
              onClick={() => onClosePeriod(period)}
            />
          )}
          <RowAction
            title="Delete"
            icon={<DeleteOutlineIcon fontSize="small" />}
            color="error"
            onClick={() => onDelete(period)}
          />
        </RowActions>
      </TableCell>
    </TableRow>
  )
}

export default PeriodRow
