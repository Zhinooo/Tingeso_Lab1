import Grid from '@mui/material/Grid'
import MenuItem from '@mui/material/MenuItem'
import TextField from '@mui/material/TextField'

function PeriodFormFields({ form, onChange }) {
  return (
    <Grid container spacing={2.5}>
      <Grid size={6}>
        <TextField
          type="number"
          label="Academic year"
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
          label="Semester"
          name="academicSemester"
          value={form.academicSemester}
          onChange={onChange}
          required
          fullWidth
        >
          <MenuItem value={1}>1 · First semester</MenuItem>
          <MenuItem value={2}>2 · Second semester</MenuItem>
        </TextField>
      </Grid>
    </Grid>
  )
}

export default PeriodFormFields
