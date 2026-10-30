import Grid from '@mui/material/Grid'
import MenuItem from '@mui/material/MenuItem'
import TextField from '@mui/material/TextField'

function PeriodFormFields({ form, onChange }) {
  return (
    <Grid container spacing={2.5}>
      <Grid size={6}>
        <TextField
          type="number"
          label="Año académico"
          name="academicYear"
          value={form.academicYear}
          onChange={onChange}
          required
          fullWidth
        />
      </Grid>
      <Grid size={6}>
        <TextField
          select
          label="Semestre"
          name="academicSemester"
          value={form.academicSemester}
          onChange={onChange}
          required
          fullWidth
        >
          <MenuItem value={1}>1 · Primer semestre</MenuItem>
          <MenuItem value={2}>2 · Segundo semestre</MenuItem>
        </TextField>
      </Grid>
    </Grid>
  )
}

export default PeriodFormFields
