import TableCell from '@mui/material/TableCell'
import TableRow from '@mui/material/TableRow'
import Typography from '@mui/material/Typography'
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutlineOutlined'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import HistoryEduOutlinedIcon from '@mui/icons-material/HistoryEduOutlined'
import RowAction from '../RowAction'
import RowActions from '../RowActions'
import StudentStatusSelect from './StudentStatusSelect'

function StudentRow({ student, onStatusChange, onDelete }) {
  return (
    <TableRow hover>
      <TableCell sx={{ fontFamily: 'monospace', whiteSpace: 'nowrap' }}>{student.studentRun}</TableCell>
      <TableCell sx={{ fontWeight: 500 }}>{student.studentFullname}</TableCell>
      <TableCell>
        <Typography variant="body2" color="text.secondary">
          {student.studentEmail}
        </Typography>
      </TableCell>
      <TableCell>{student.careerCode}</TableCell>
      <TableCell>{student.studyplanIdentifier}</TableCell>
      <TableCell>
        <StudentStatusSelect
          status={student.studentStatus}
          onChange={(newStatus) => onStatusChange(student, newStatus)}
        />
      </TableCell>
      <TableCell align="right">
        <RowActions>
          <RowAction
            title="History"
            icon={<HistoryEduOutlinedIcon fontSize="small" />}
            color="primary"
            to={`/admin/students/${student.studentId}/history`}
          />
          <RowAction
            title="Edit"
            icon={<EditOutlinedIcon fontSize="small" />}
            to={`/admin/students/${student.studentId}/edit`}
          />
          <RowAction
            title="Delete"
            icon={<DeleteOutlineIcon fontSize="small" />}
            color="error"
            onClick={() => onDelete(student)}
          />
        </RowActions>
      </TableCell>
    </TableRow>
  )
}

export default StudentRow
