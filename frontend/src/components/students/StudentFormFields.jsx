import Grid from '@mui/material/Grid'
import MenuItem from '@mui/material/MenuItem'
import TextField from '@mui/material/TextField'
import { MIN_PASSWORD_LENGTH, RUN_HELPER_TEXT } from '../../constants/accounts'

function StudentFormFields({ form, careers, isEdit, onChange }) {
  return (
    <Grid container spacing={2.5}>
      <Grid size={6}>
        <TextField
          label="RUN"
          name="studentRun"
          value={form.studentRun}
          onChange={onChange}
          required
          fullWidth
          disabled={isEdit}
          helperText={isEdit ? 'The RUN cannot be changed' : RUN_HELPER_TEXT}
        />
      </Grid>
      <Grid size={6}>
        <TextField
          label="Full name"
          name="studentFullname"
          value={form.studentFullname}
          onChange={onChange}
          required
          fullWidth
          helperText=" "
        />
      </Grid>
      <Grid size={6}>
        <TextField
          type="email"
          label="Email"
          name="studentEmail"
          value={form.studentEmail}
          onChange={onChange}
          required
          fullWidth
        />
      </Grid>
      {!isEdit && (
        <>
          <Grid size={6}>
            <TextField
              select
              label="Career"
              name="careerCode"
              value={form.careerCode}
              onChange={onChange}
              required
              fullWidth
              helperText="Only active careers. The current study plan is assigned automatically."
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
              type="password"
              label="Temporary password"
              name="password"
              value={form.password}
              onChange={onChange}
              required
              fullWidth
              helperText={`At least ${MIN_PASSWORD_LENGTH} characters. The student will change it on first login.`}
            />
          </Grid>
        </>
      )}
    </Grid>
  )
}

export default StudentFormFields
