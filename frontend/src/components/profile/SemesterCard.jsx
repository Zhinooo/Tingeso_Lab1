import Card from '@mui/material/Card'
import List from '@mui/material/List'
import ListItem from '@mui/material/ListItem'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import { SEMESTER_COLORS } from '../../constants/semesters'
import PrerequisiteChips from '../courses/PrerequisiteChips'

function SemesterCard({ semester, courses }) {
  const color = SEMESTER_COLORS[semester]
  const totalSct = courses.reduce((sum, course) => sum + course.sct, 0)

  return (
    <Card variant="outlined" sx={{ height: '100%', borderTop: `4px solid ${color}` }}>
      <Stack direction="row" sx={{ justifyContent: 'space-between', alignItems: 'center', px: 2, pt: 2, pb: 1 }}>
        <Typography sx={{ fontWeight: 700, color }}>Semestre {semester}</Typography>
        <Typography variant="caption" color="text.secondary">
          {totalSct} SCT
        </Typography>
      </Stack>
      <List dense disablePadding>
        {courses.map((course) => (
          <ListItem key={course.courseId} divider sx={{ display: 'block', py: 1.25 }}>
            <Stack direction="row" sx={{ justifyContent: 'space-between', gap: 1 }}>
              <Typography variant="body2" sx={{ fontWeight: 600 }}>
                {course.courseName}
              </Typography>
              <Typography variant="caption" color="text.secondary" sx={{ whiteSpace: 'nowrap' }}>
                {course.sct} SCT
              </Typography>
            </Stack>
            <Stack direction="row" sx={{ gap: 0.75, alignItems: 'center', mt: 0.5 }}>
              <Typography variant="caption" sx={{ fontFamily: 'monospace' }}>
                {course.courseCode}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                TEL {`${course.theoryHours}-${course.exerciseHours}-${course.labHours}`}
              </Typography>
            </Stack>
            {course.prerequisiteCodes.length > 0 && (
              <Stack sx={{ mt: 0.5 }}>
                <PrerequisiteChips codes={course.prerequisiteCodes} />
              </Stack>
            )}
          </ListItem>
        ))}
      </List>
    </Card>
  )
}

export default SemesterCard
