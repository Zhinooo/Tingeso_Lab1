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
          helperText={isEdit ? 'El RUN no se puede cambiar' : RUN_HELPER_TEXT}
        />
      </Grid>
      <Grid size={6}>
        <TextField
          label="Nombre completo"
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
          label="Correo"
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
              label="Carrera"
              name="careerCode"
              value={form.careerCode}
              onChange={onChange}
              required
              fullWidth
              helperText="Solo carreras activas. El plan vigente se asigna automáticamente."
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
              label="Contraseña temporal"
              name="password"
              value={form.password}
              onChange={onChange}
              required
              fullWidth
              helperText={`Mínimo ${MIN_PASSWORD_LENGTH} caracteres. El estudiante la cambiará al iniciar sesión por primera vez.`}
            />
          </Grid>
        </>
      )}
    </Grid>
  )
}

export default StudentFormFields
