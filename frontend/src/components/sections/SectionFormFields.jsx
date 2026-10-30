import Grid from '@mui/material/Grid'
import MenuItem from '@mui/material/MenuItem'
import TextField from '@mui/material/TextField'
import { MAX_CAPACITY, MIN_CAPACITY } from '../../constants/sections'
import { formatPeriod } from '../../utils/periods'

function SectionFormFields({ form, periods, courses, teachers, isEdit, onChange }) {
  return (
    <Grid container spacing={2.5}>
      <Grid size={6}>
        <TextField
          select
          label="Período académico"
          name="academicPeriodId"
          value={form.academicPeriodId}
          onChange={onChange}
          required
          fullWidth
          disabled={isEdit}
          helperText={isEdit ? 'El período no se puede cambiar' : 'Solo períodos abiertos'}
        >
          {periods.map((period) => (
            <MenuItem key={period.academicPeriodId} value={String(period.academicPeriodId)}>
              {formatPeriod(period)}
            </MenuItem>
          ))}
        </TextField>
      </Grid>
      <Grid size={6}>
        <TextField
          select
          label="Ramo"
          name="courseId"
          value={form.courseId}
          onChange={onChange}
          required
          fullWidth
          disabled={isEdit}
          helperText={isEdit ? 'El ramo no se puede cambiar' : ' '}
        >
          {courses.map((course) => (
            <MenuItem key={course.courseId} value={String(course.courseId)}>
              {course.courseCode} · {course.courseName} ({course.studyplanIdentifier})
            </MenuItem>
          ))}
        </TextField>
      </Grid>
      <Grid size={6}>
        <TextField
          select
          label="Docente"
          name="teacherId"
          value={form.teacherId}
          onChange={onChange}
          required
          fullWidth
          helperText="Solo docentes activos"
        >
          {teachers.map((teacher) => (
            <MenuItem key={teacher.teacherId} value={String(teacher.teacherId)}>
              {teacher.teacherFullname}
            </MenuItem>
          ))}
        </TextField>
      </Grid>
      <Grid size={6}>
        <TextField
          type="number"
          label="Cupos"
          name="sectionCapacity"
          value={form.sectionCapacity}
          onChange={onChange}
          required
          fullWidth
          helperText={`Entre ${MIN_CAPACITY} y ${MAX_CAPACITY}`}
          slotProps={{ htmlInput: { min: MIN_CAPACITY, max: MAX_CAPACITY } }}
        />
      </Grid>
    </Grid>
  )
}

export default SectionFormFields
