import Grid from '@mui/material/Grid'
import TextField from '@mui/material/TextField'

function CareerFormFields({ form, onChange }) {
  return (
    <Grid container spacing={2.5}>
      <Grid size={4}>
        <TextField
          label="Código de carrera"
          name="careerCode"
          value={form.careerCode}
          onChange={onChange}
          required
          fullWidth
          helperText="Código corto y único, ej. 10450"
        />
      </Grid>
      <Grid size={8}>
        <TextField
          label="Nombre de la carrera"
          name="careerName"
          value={form.careerName}
          onChange={onChange}
          required
          fullWidth
        />
      </Grid>
      <Grid size={12}>
        <TextField
          label="Descripción"
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