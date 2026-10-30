import Grid from '@mui/material/Grid'
import TextField from '@mui/material/TextField'

function CareerFormFields({ form, onChange }) {
  return (
    <Grid container spacing={2.5}>
      <Grid size={4}>
        <TextField
          label="Career code"
          name="careerCode"
          value={form.careerCode}
          onChange={onChange}
          required
          fullWidth
          helperText="Short unique code, e.g. TIN"
        />
      </Grid>
      <Grid size={8}>
        <TextField
          label="Career name"
          name="careerName"
          value={form.careerName}
          onChange={onChange}
          required
          fullWidth
        />
      </Grid>
      <Grid size={12}>
        <TextField
          label="Description"
          name="careerDescription"
          value={form.careerDescription}
          onChange={onChange}
          fullWidth
          multiline
          minRows={4}
        />
      </Grid>
    </Grid>
  )
}

export default CareerFormFields