import Grid from '@mui/material/Grid'
import MenuItem from '@mui/material/MenuItem'
import TextField from '@mui/material/TextField'
import { SEMESTERS } from '../../constants/semesters'

function CourseFormFields({ form, studyplans, isEdit, onChange }) {
  return (
    <Grid container spacing={2.5}>
      <Grid size={6}>
        <TextField
          select
          label="Plan de estudio"
          name="studyplanId"
          value={form.studyplanId}
          onChange={onChange}
          required
          fullWidth
          disabled={isEdit}
          helperText={isEdit ? 'El plan de estudio no se puede cambiar' : ' '}
        >
          {studyplans.map((studyplan) => (
            <MenuItem key={studyplan.studyplanId} value={String(studyplan.studyplanId)}>
              {studyplan.careerCode} · {studyplan.studyplanIdentifier}
            </MenuItem>
          ))}
        </TextField>
      </Grid>
      <Grid size={6}>
        <TextField
          select
          label="Semestre"
          name="semester"
          value={form.semester}
          onChange={onChange}
          required
          fullWidth
          helperText=" "
        >
          {SEMESTERS.map((semester) => (
            <MenuItem key={semester} value={semester}>
             Semestre {semester}
            </MenuItem>
          ))}
        </TextField>
      </Grid>
      <Grid size={4}>
        <TextField
          label="Código del ramo"
          name="courseCode"
          value={form.courseCode}
          onChange={onChange}
          required
          fullWidth
        />
      </Grid>
      <Grid size={8}>
        <TextField
          label="Nombre del ramo"
          name="courseName"
          value={form.courseName}
          onChange={onChange}
          required
          fullWidth
        />
      </Grid>
    </Grid>
  )
}

export default CourseFormFields
