import Grid from '@mui/material/Grid'
import TextField from '@mui/material/TextField'
import { MIN_PASSWORD_LENGTH, RUN_HELPER_TEXT } from '../../constants/accounts'

function TeacherFormFields({ form, isEdit, onChange }) {
  return (
    <Grid container spacing={2.5}>
      <Grid size={6}>
        <TextField
          label="RUN"
          name="teacherRun"
          value={form.teacherRun}
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
          name="teacherFullname"
          value={form.teacherFullname}
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
          name="teacherEmail"
          value={form.teacherEmail}
          onChange={onChange}
          required
          fullWidth
        />
      </Grid>
      <Grid size={6}>
        <TextField
          label="Professional title"
          name="teacherProtitle"
          value={form.teacherProtitle}
          onChange={onChange}
          required
          fullWidth
        />
      </Grid>
      <Grid size={6}>
        <TextField
          label="Academic degree"
          name="teacherDegree"
          value={form.teacherDegree}
          onChange={onChange}
          required
          fullWidth
        />
      </Grid>
      {!isEdit && (
        <Grid size={6}>
          <TextField
            type="password"
            label="Temporary password"
            name="password"
            value={form.password}
            onChange={onChange}
            required
            fullWidth
            helperText={`At least ${MIN_PASSWORD_LENGTH} characters. The teacher will change it on first login.`}
          />
        </Grid>
      )}
    </Grid>
  )
}

export default TeacherFormFields
