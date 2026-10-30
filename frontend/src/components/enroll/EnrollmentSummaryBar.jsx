import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Chip from '@mui/material/Chip'
import Paper from '@mui/material/Paper'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import { MAX_COURSES, MIN_COURSES } from '../../constants/enrollments'

function selectionColor(count) {
  if (count > MAX_COURSES) return 'error'
  return count >= MIN_COURSES ? 'success' : 'default'
}

function EnrollmentSummaryBar({ count, saving, onSubmit }) {
  return (
    <Paper sx={{ position: 'sticky', bottom: 16, mt: 2, p: 2, boxShadow: '0 8px 24px rgba(15, 39, 71, 0.08)' }}>
      <Stack direction="row" spacing={2} sx={{ alignItems: 'center', justifyContent: 'space-between' }}>
        <Box>
          <Chip label={`${count} selected`} color={selectionColor(count)} sx={{ mr: 1 }} />
          <Typography component="span" variant="body2" color="text.secondary">
            Select between {MIN_COURSES} and {MAX_COURSES} courses per period (fewer only if fewer are available).
          </Typography>
        </Box>
        <Button
          variant="contained"
          size="large"
          onClick={onSubmit}
          disabled={count === 0 || count > MAX_COURSES || saving}
        >
          Enroll
        </Button>
      </Stack>
    </Paper>
  )
}

export default EnrollmentSummaryBar
