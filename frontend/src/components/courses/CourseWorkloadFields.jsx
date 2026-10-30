import Grid from '@mui/material/Grid'
import Stack from '@mui/material/Stack'
import TextField from '@mui/material/TextField'
import Typography from '@mui/material/Typography'
import { MAX_SCT, MAX_WEEKLY_HOURS_EXCLUSIVE } from '../../constants/courses'
import ValidationRule from '../ValidationRule'

const HOUR_FIELDS = [
  { name: 'theoryHours', label: 'Horas de teoría' },
  { name: 'exerciseHours', label: 'Horas de ejercicios' },
  { name: 'labHours', label: 'Horas de laboratorio' },
]

function CourseWorkloadFields({ form, onChange }) {
  const totalHours = Number(form.theoryHours) + Number(form.exerciseHours) + Number(form.labHours)
  const sct = Number(form.sct)

  return (
    <>
      <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 2 }}>
       Carga académica
      </Typography>
      <Grid container spacing={2.5}>
        {HOUR_FIELDS.map((field) => (
          <Grid key={field.name} size={3}>
            <TextField
              type="number"
              label={field.label}
              name={field.name}
              value={form[field.name]}
              onChange={onChange}
              fullWidth
              slotProps={{ htmlInput: { min: 0 } }}
            />
          </Grid>
        ))}
        <Grid size={3}>
          <TextField
            type="number"
            label="Créditos SCT"
            name="sct"
            value={form.sct}
            onChange={onChange}
            fullWidth
            slotProps={{ htmlInput: { min: 1, max: MAX_SCT } }}
          />
        </Grid>
      </Grid>
      <Stack direction="row" spacing={3} sx={{ mt: 2, p: 1.5, borderRadius: 2, bgcolor: 'grey.50' }}>
        <ValidationRule ok={totalHours < MAX_WEEKLY_HOURS_EXCLUSIVE}>
          Horas totales: {totalHours} (deben ser menos de {MAX_WEEKLY_HOURS_EXCLUSIVE})
        </ValidationRule>
        <ValidationRule ok={sct >= 1 && sct <= MAX_SCT}>SCT entre 1 y {MAX_SCT}</ValidationRule>
      </Stack>
    </>
  )
}

export default CourseWorkloadFields
