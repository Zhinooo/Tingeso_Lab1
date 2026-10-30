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
      <Typography variant="h6">Cambiar carrera y plan de estudio</Typography>
      <Alert severity="warning" sx={{ my: 2 }}>
        Solo se permite mientras el estudiante no tenga inscripciones ni historial académico.
      </Alert>
      <ErrorAlert message={error} />
      <Grid container spacing={2.5}>
        <Grid size={6}>
          <TextField
            select
            label="Carrera"
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
            label="Plan de estudio"
            name="studyplanIdentifier"
            value={careerForm.studyplanIdentifier}
            onChange={onChange}
            fullWidth
          >
            <MenuItem value="">Plan vigente de la carrera</MenuItem>
            {careerStudyplans.map((studyplan) => (
              <MenuItem key={studyplan.studyplanId} value={studyplan.studyplanIdentifier}>
                {studyplan.studyplanIdentifier}
                {studyplan.studyplanStatus === 'CURRENT' ? ' (vigente)' : ''}
              </MenuItem>
            ))}
          </TextField>
        </Grid>
      </Grid>
      <Stack direction="row" sx={{ justifyContent: 'flex-end', mt: 3 }}>
        <Button type="submit" variant="outlined" color="warning" disabled={saving}>
         Cambiar carrera
        </Button>
      </Stack>
    </Paper>
  )
}

export default StudentCareerChangeCard
