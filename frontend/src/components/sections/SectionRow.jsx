import TableCell from '@mui/material/TableCell'
import TableRow from '@mui/material/TableRow'
import Typography from '@mui/material/Typography'
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutlineOutlined'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import { formatPeriod } from '../../utils/periods'
import RowAction from '../RowAction'
import RowActions from '../RowActions'
import ScheduleChips from '../ScheduleChips'

function SectionRow({ section, onDelete }) {
  return (
    <TableRow hover>
      <TableCell sx={{ whiteSpace: 'nowrap' }}>{formatPeriod(section)}</TableCell>
      <TableCell>
        <Typography variant="body2" sx={{ fontWeight: 700, fontFamily: 'monospace' }}>
          {section.courseCode}
        </Typography>
        <Typography variant="body2" color="text.secondary">
          {section.courseName}
        </Typography>
      </TableCell>
      <TableCell>{section.teacherFullname}</TableCell>
      <TableCell align="center">{section.sectionCapacity}</TableCell>
      <TableCell>
        <ScheduleChips blocks={section.scheduleBlocks} />
      </TableCell>
      <TableCell align="right">
        <RowActions>
          <RowAction
            title="Edit"
            icon={<EditOutlinedIcon fontSize="small" />}
            to={`/admin/sections/${section.sectionId}/edit`}
          />
          <RowAction
            title="Delete"
            icon={<DeleteOutlineIcon fontSize="small" />}
            color="error"
            onClick={() => onDelete(section)}
          />
        </RowActions>
      </TableCell>
    </TableRow>
  )
}

export default SectionRow
