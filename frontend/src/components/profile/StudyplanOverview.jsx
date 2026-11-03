import Grid from '@mui/material/Grid'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import { SEMESTERS } from '../../constants/semesters'
import SemesterCard from './SemesterCard'

function StudyplanOverview({ courses, action }) {
  return (
    <>
      <Stack direction="row" sx={{ justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h6">Plan de estudio</Typography>
        {action}
      </Stack>
      <Grid container spacing={2}>
        {SEMESTERS.map((semester) => (
          <Grid key={semester} size={3}>
            <SemesterCard semester={semester} courses={courses.filter((course) => course.semester === semester)} />
          </Grid>
        ))}
      </Grid>
    </>
  )
}

export default StudyplanOverview
