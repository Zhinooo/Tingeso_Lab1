import Avatar from '@mui/material/Avatar'
import Box from '@mui/material/Box'
import Divider from '@mui/material/Divider'
import Grid from '@mui/material/Grid'
import Paper from '@mui/material/Paper'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import { getInitials } from '../../utils/names'
import Stat from '../Stat'
import StatusChip from '../StatusChip'

function ProfileSummary({ student, careerName }) {
  return (
    <Paper sx={{ p: 3, mb: 4 }}>
      <Stack direction="row" spacing={3} sx={{ alignItems: 'center' }}>
        <Stack direction="row" spacing={2} sx={{ alignItems: 'center', minWidth: 260 }}>
          <Avatar sx={{ width: 56, height: 56, bgcolor: 'primary.main', fontSize: 20 }}>
            {getInitials(student.studentFullname)}
          </Avatar>
          <Box>
            <Typography variant="h6">{student.studentFullname}</Typography>
            <StatusChip status={student.studentStatus} />
          </Box>
        </Stack>
        <Divider flexItem orientation="vertical" />
        <Grid container spacing={2} sx={{ flex: 1 }}>
          <Grid size={3}>
            <Stat label="RUN" value={student.studentRun} />
          </Grid>
          <Grid size={3}>
            <Stat label="Correo" value={student.studentEmail} />
          </Grid>
          <Grid size={3}>
            <Stat label="Carrera" value={careerName ?? student.careerCode} />
          </Grid>
          <Grid size={3}>
            <Stat label="Plan de estudio" value={student.studyplanIdentifier} />
          </Grid>
        </Grid>
      </Stack>
    </Paper>
  )
}

export default ProfileSummary
