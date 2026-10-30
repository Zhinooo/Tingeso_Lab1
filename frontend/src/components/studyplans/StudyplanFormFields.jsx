import Checkbox from '@mui/material/Checkbox'
import FormControlLabel from '@mui/material/FormControlLabel'
import Grid from '@mui/material/Grid'
import MenuItem from '@mui/material/MenuItem'
import TextField from '@mui/material/TextField'

function StudyplanFormFields({ form, careers, isEdit, onChange }) {
  return (
    <Grid container spacing={2.5}>
      <Grid size={6}>
        <TextField
          label="Identificador"
          name="studyplanIdentifier"
          value={form.studyplanIdentifier}
          onChange={onChange}
          required
          fullWidth
          helperText="Ej. 2021.3"
        />
      </Grid>
      <Grid size={6}>
        <TextField
          select
          label="Carrera"
          name="careerCode"
          value={form.careerCode}
          onChange={onChange}
          required
          fullWidth
          disabled={isEdit}
          helperText={isEdit ? 'La carrera no se puede cambiar' : ' '}
        >
          {careers.map((career) => (
            <MenuItem key={career.careerId} value={career.careerCode}>
              {career.careerCode} · {career.careerName}
            </MenuItem>
          ))}
        </TextField>
      </Grid>
      {!isEdit && (
        <Grid size={12}>
          <FormControlLabel
            control={<Checkbox name="current" checked={form.current} onChange={onChange} />}
            label="Plan vigente (los nuevos estudiantes quedarán en este plan)"
          />
        </Grid>
      )}
    </Grid>
  )
}

export default StudyplanFormFields