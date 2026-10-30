import Box from '@mui/material/Box'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import { SEMESTER_COLORS, SEMESTERS } from '../../constants/semesters'

function SemesterLegend() {
  return (
    <Stack direction="row" spacing={1.5}>
      {SEMESTERS.map((semester) => (
        <Stack key={semester} direction="row" spacing={0.75} sx={{ alignItems: 'center' }}>
          <Box sx={{ width: 10, height: 10, borderRadius: '50%', bgcolor: SEMESTER_COLORS[semester] }} />
          <Typography variant="caption" color="text.secondary">
            Semester {semester}
          </Typography>
        </Stack>
      ))}
    </Stack>
  )
}

export default SemesterLegend
