import TableCell from '@mui/material/TableCell'
import TableRow from '@mui/material/TableRow'
import Typography from '@mui/material/Typography'
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutlineOutlined'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import RowAction from '../RowAction'
import RowActions from '../RowActions'
import StatusChip from '../StatusChip'
import StatusToggleAction from '../StatusToggleAction'

function TeacherRow({ teacher, onToggleStatus, onDelete }) {
  return (
    <TableRow hover>
      <TableCell sx={{ fontFamily: 'monospace', whiteSpace: 'nowrap' }}>{teacher.teacherRun}</TableCell>
      <TableCell sx={{ fontWeight: 500 }}>{teacher.teacherFullname}</TableCell>
      <TableCell>
        <Typography variant="body2" color="text.secondary">
          {teacher.teacherEmail}
        </Typography>
      </TableCell>
      <TableCell>{teacher.teacherProtitle}</TableCell>
      <TableCell>{teacher.teacherDegree}</TableCell>
      <TableCell>
        <StatusChip status={teacher.teacherStatus} />
      </TableCell>
      <TableCell align="right">
        <RowActions>
          <RowAction
            title="Edit"
            icon={<EditOutlinedIcon fontSize="small" />}
            to={`/admin/teachers/${teacher.teacherId}/edit`}
          />
          <StatusToggleAction active={teacher.teacherStatus === 'ACTIVE'} onClick={() => onToggleStatus(teacher)} />
          <RowAction
            title="Delete"
            icon={<DeleteOutlineIcon fontSize="small" />}
            color="error"
            onClick={() => onDelete(teacher)}
          />
        </RowActions>
      </TableCell>
    </TableRow>
  )
}

export default TeacherRow
