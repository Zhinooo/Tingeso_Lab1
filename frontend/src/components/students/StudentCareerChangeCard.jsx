import Alert from '@mui/material/Alert'
import Button from '@mui/material/Button'
import Grid from '@mui/material/Grid'
import MenuItem from '@mui/material/MenuItem'
import Paper from '@mui/material/Paper'
import Stack from '@mui/material/Stack'
import TextField from '@mui/material/TextField'
import Typography from '@mui/material/Typography'
import ErrorAlert from '../ErrorAlert'

function StudentCareerChangeCard({ careerForm, careers, studyplans, error, saving, onChange, onSubmit }) {
  const careerStudyplans = studyplans.filter((studyplan) => studyplan.careerCode === careerForm.careerCode)

  return (
    <Paper component="form" onSubmit={onSubmit} noValidate sx={{ p: 4, mt: 3 }}>
      <Typography variant="h6">Change career and study plan</Typography>
      <Alert severity="warning" sx={{ my: 2 }}>
        Only allowed while the student has no enrollments or academic history.
      </Alert>
      <ErrorAlert message={error} />
      <Grid container spacing={2.5}>
        <Grid size={6}>
          <TextField
            select
            label="Career"
            name="careerCode"
            value={careerForm.careerCode}
            onChange={onChange}
            fullWidth
          >
            {careers.map((career) => (
              <MenuItem key={career.careerId} value={career.careerCode}>
                {career.careerCode} · {career.careerName}
              </MenuItem>
            ))}
          </TextField>
        </Grid>
        <Grid size={6}>
          <TextField
            select
            label="Study plan"
            name="studyplanIdentifier"
            value={careerForm.studyplanIdentifier}
            onChange={onChange}
            fullWidth
          >
            <MenuItem value="">Current plan of the career</MenuItem>
            {careerStudyplans.map((studyplan) => (
              <MenuItem key={studyplan.studyplanId} value={studyplan.studyplanIdentifier}>
                {studyplan.studyplanIdentifier}
                {studyplan.studyplanStatus === 'CURRENT' ? ' (current)' : ''}
              </MenuItem>
            ))}
          </TextField>
        </Grid>
      </Grid>
      <Stack direction="row" sx={{ justifyContent: 'flex-end', mt: 3 }}>
        <Button type="submit" variant="outlined" color="warning" disabled={saving}>
          Change career
        </Button>
      </Stack>
    </Paper>
  )
}

export default StudentCareerChangeCard
