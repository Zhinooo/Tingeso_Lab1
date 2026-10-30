import TableCell from '@mui/material/TableCell'
import TableRow from '@mui/material/TableRow'
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutlineOutlined'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import MenuBookOutlinedIcon from '@mui/icons-material/MenuBookOutlined'
import StarIcon from '@mui/icons-material/Star'
import StarOutlineIcon from '@mui/icons-material/StarOutlineOutlined'
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
            title="Courses"
            icon={<MenuBookOutlinedIcon fontSize="small" />}
            color="primary"
            to={`/admin/courses?studyplanId=${studyplan.studyplanId}`}
          />
          <RowAction
            title="Edit"
            icon={<EditOutlinedIcon fontSize="small" />}
            to={`/admin/studyplans/${studyplan.studyplanId}/edit`}
          />
          <RowAction
            title={isCurrent ? 'Set not current' : 'Set current'}
            icon={isCurrent ? <StarIcon fontSize="small" /> : <StarOutlineIcon fontSize="small" />}
            color={isCurrent ? 'warning' : 'default'}
            onClick={() => onToggleStatus(studyplan)}
          />
          <RowAction
            title="Delete"
            icon={<DeleteOutlineIcon fontSize="small" />}
            color="error"
            onClick={() => onDelete(studyplan)}
          />
        </RowActions>
      </TableCell>
    </TableRow>
  )
}

export default StudyplanRow