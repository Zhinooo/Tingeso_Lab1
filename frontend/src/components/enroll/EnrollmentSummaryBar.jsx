import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Chip from '@mui/material/Chip'
import Paper from '@mui/material/Paper'
import Stack from '@mui/material/Stack'
import EnrollmentChecks from './EnrollmentChecks'

function EnrollmentSummaryBar({ count, checks, saving, blocked, onSubmit }) {
  const hasProblems = checks.some((check) => !check.ok)

  return (
    <Paper sx={{ position: 'sticky', bottom: 16, mt: 2, p: 2 }}>
      <Stack direction="row" spacing={2} sx={{ alignItems: 'center', justifyContent: 'space-between' }}>
        <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }}>
          <Chip label={`${count} seleccionados`} color={count > 0 && !hasProblems ? 'success' : 'default'} />
          <Box>{count > 0 && <EnrollmentChecks checks={checks} />}</Box>
        </Stack>
        <Button
          variant="contained"
          size="large"
          onClick={onSubmit}
          disabled={blocked || count === 0 || hasProblems || saving}
        >
         Inscribir
        </Button>
      </Stack>
    </Paper>
  )
}

export default EnrollmentSummaryBar
