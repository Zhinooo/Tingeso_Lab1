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
          helperText={isEdit ? 'El RUN no se puede cambiar' : RUN_HELPER_TEXT}
        />
      </Grid>
      <Grid size={6}>
        <TextField
          label="Nombre completo"
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
          label="Correo"
          name="teacherEmail"
          value={form.teacherEmail}
          onChange={onChange}
          required
          fullWidth
        />
      </Grid>
      <Grid size={6}>
        <TextField
          label="Título profesional"
          name="teacherProtitle"
          value={form.teacherProtitle}
          onChange={onChange}
          required
          fullWidth
        />
      </Grid>
      <Grid size={6}>
        <TextField
          label="Grado académico"
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
            label="Contraseña temporal"
            name="password"
            value={form.password}
            onChange={onChange}
            required
            fullWidth
            helperText={`Mínimo ${MIN_PASSWORD_LENGTH} caracteres. El docente la cambiará al iniciar sesión por primera vez.`}
          />
        </Grid>
      )}
    </Grid>
  )
}

export default TeacherFormFields
