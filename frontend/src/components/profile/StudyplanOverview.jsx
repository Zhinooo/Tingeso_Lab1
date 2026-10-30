import Grid from '@mui/material/Grid'
import Typography from '@mui/material/Typography'
import { SEMESTERS } from '../../constants/semesters'
import SemesterCard from './SemesterCard'

function StudyplanOverview({ courses }) {
  return (
    <>
      <Typography variant="h6" sx={{ mb: 2 }}>
       Plan de estudio
      </Typography>
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
