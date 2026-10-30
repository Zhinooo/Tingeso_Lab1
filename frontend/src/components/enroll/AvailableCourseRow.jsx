import Checkbox from '@mui/material/Checkbox'
import TableCell from '@mui/material/TableCell'
import TableRow from '@mui/material/TableRow'
import Typography from '@mui/material/Typography'
import ScheduleChips from '../ScheduleChips'
import SemesterBadge from '../courses/SemesterBadge'
import SeatsIndicator from './SeatsIndicator'

function AvailableCourseRow({ course, checked, onToggle }) {
  const isFull = course.availableSeats <= 0

  return (
    <TableRow
      hover={!isFull}
      selected={checked}
      onClick={() => !isFull && onToggle(course.courseId)}
      sx={{ cursor: isFull ? 'not-allowed' : 'pointer', opacity: isFull ? 0.55 : 1 }}
    >
      <TableCell padding="checkbox">
        <Checkbox checked={checked} disabled={isFull} />
      </TableCell>
      <TableCell>
        <SemesterBadge semester={course.semester} />
      </TableCell>
      <TableCell>
        <Typography variant="body2" sx={{ fontWeight: 700, fontFamily: 'monospace' }}>
          {course.courseCode}
        </Typography>
        <Typography variant="body2">{course.courseName}</Typography>
      </TableCell>
      <TableCell>{course.teacherFullname}</TableCell>
      <TableCell>
        <ScheduleChips blocks={course.scheduleBlocks} />
      </TableCell>
      <TableCell sx={{ minWidth: 110 }}>
        <SeatsIndicator available={course.availableSeats} capacity={course.sectionCapacity} />
      </TableCell>
    </TableRow>
  )
}

export default AvailableCourseRow
