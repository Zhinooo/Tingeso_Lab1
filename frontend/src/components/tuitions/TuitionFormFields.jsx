import Grid from '@mui/material/Grid'
import MenuItem from '@mui/material/MenuItem'
import TextField from '@mui/material/TextField'
import { formatPeriod } from '../../utils/periods'

function TuitionFormFields({ form, students, periods, onChange }) {
  return (
    <Grid container spacing={2.5}>
      <Grid size={6}>
        <TextField
          select
          label="Student"
          name="studentId"
          value={form.studentId}
          onChange={onChange}
          required
          fullWidth
          helperText="Graduated and expelled students are not listed"
        >
          {students.map((student) => (
            <MenuItem key={student.studentId} value={String(student.studentId)}>
              {student.studentRun} · {student.studentFullname}
            </MenuItem>
          ))}
        </TextField>
      </Grid>
      <Grid size={6}>
        <TextField
          select
          label="Academic period"
          name="academicPeriodId"
          value={form.academicPeriodId}
          onChange={onChange}
          required
          fullWidth
          helperText="Only open periods"
        >
          {periods.map((period) => (
            <MenuItem key={period.academicPeriodId} value={String(period.academicPeriodId)}>
              {formatPeriod(period)}
            </MenuItem>
          ))}
        </TextField>
      </Grid>
    </Grid>
  )
}

export default TuitionFormFields
