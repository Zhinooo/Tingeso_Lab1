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
          label="Identifier"
          name="studyplanIdentifier"
          value={form.studyplanIdentifier}
          onChange={onChange}
          required
          fullWidth
          helperText="e.g. TIN-2025"
        />
      </Grid>
      <Grid size={6}>
        <TextField
          select
          label="Career"
          name="careerCode"
          value={form.careerCode}
          onChange={onChange}
          required
          fullWidth
          disabled={isEdit}
          helperText={isEdit ? 'The career cannot be changed' : ' '}
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
            label="Current plan (new students will be assigned to it)"
          />
        </Grid>
      )}
    </Grid>
  )
}

export default StudyplanFormFields