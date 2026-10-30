import TableCell from '@mui/material/TableCell'
import TableRow from '@mui/material/TableRow'
import Typography from '@mui/material/Typography'
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutlineOutlined'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import { SEMESTER_COLORS } from '../../constants/semesters'
import RowAction from '../RowAction'
import RowActions from '../RowActions'
import PrerequisiteChips from './PrerequisiteChips'
import SemesterBadge from './SemesterBadge'

function CourseRow({ course, onDelete }) {
  return (
    <TableRow
      hover
      sx={{ '& td:first-of-type': { borderLeft: `4px solid ${SEMESTER_COLORS[course.semester]}` } }}
    >
      <TableCell>
        <SemesterBadge semester={course.semester} />
      </TableCell>
      <TableCell sx={{ fontWeight: 700, fontFamily: 'monospace' }}>{course.courseCode}</TableCell>
      <TableCell sx={{ fontWeight: 500 }}>{course.courseName}</TableCell>
      <TableCell sx={{ fontFamily: 'monospace' }}>
        {`${course.theoryHours}-${course.exerciseHours}-${course.labHours}`}
      </TableCell>
      <TableCell>{course.sct}</TableCell>
      <TableCell>
        <Typography variant="body2" color="text.secondary">
          {course.studyplanIdentifier}
        </Typography>
      </TableCell>
      <TableCell>
        <PrerequisiteChips codes={course.prerequisiteCodes} />
      </TableCell>
      <TableCell align="right">
        <RowActions>
          <RowAction
            title="Edit"
            icon={<EditOutlinedIcon fontSize="small" />}
            to={`/admin/courses/${course.courseId}/edit`}
          />
          <RowAction
            title="Delete"
            icon={<DeleteOutlineIcon fontSize="small" />}
            color="error"
            onClick={() => onDelete(course)}
          />
        </RowActions>
      </TableCell>
    </TableRow>
  )
}

export default CourseRow
