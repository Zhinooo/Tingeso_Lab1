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

function EnrollmentSummaryBar({ count, saving, blocked, onSubmit }) {
  return (
    <Paper sx={{ position: 'sticky', bottom: 16, mt: 2, p: 2 }}>
      <Stack direction="row" spacing={2} sx={{ alignItems: 'center', justifyContent: 'space-between' }}>
        <Box>
          <Chip label={`${count} seleccionados`} color={selectionColor(count)} sx={{ mr: 1 }} />
          <Typography component="span" variant="body2" color="text.secondary">
            Elige entre {MIN_COURSES} y {MAX_COURSES} ramos por período (menos solo si hay menos disponibles).
          </Typography>
        </Box>
        <Button
          variant="contained"
          size="large"
          onClick={onSubmit}
          disabled={blocked || count === 0 || count > MAX_COURSES || saving}
        >
         Inscribir
        </Button>
      </Stack>
    </Paper>
  )
}

export default EnrollmentSummaryBar
