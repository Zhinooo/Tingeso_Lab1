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
          label="Study plan"
          name="studyplanId"
          value={form.studyplanId}
          onChange={onChange}
          required
          fullWidth
          disabled={isEdit}
          helperText={isEdit ? 'The study plan cannot be changed' : ' '}
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
          label="Semester"
          name="semester"
          value={form.semester}
          onChange={onChange}
          required
          fullWidth
          helperText=" "
        >
          {SEMESTERS.map((semester) => (
            <MenuItem key={semester} value={semester}>
              Semester {semester}
            </MenuItem>
          ))}
        </TextField>
      </Grid>
      <Grid size={4}>
        <TextField
          label="Course code"
          name="courseCode"
          value={form.courseCode}
          onChange={onChange}
          required
          fullWidth
        />
      </Grid>
      <Grid size={8}>
        <TextField
          label="Course name"
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
