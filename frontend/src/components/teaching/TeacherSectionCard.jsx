import { Link as RouterLink } from 'react-router-dom'
import Button from '@mui/material/Button'
import Card from '@mui/material/Card'
import CardActions from '@mui/material/CardActions'
import CardContent from '@mui/material/CardContent'
import Chip from '@mui/material/Chip'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import GroupsOutlinedIcon from '@mui/icons-material/GroupsOutlined'
import { formatPeriod } from '../../utils/periods'
import ScheduleChips from '../ScheduleChips'

function TeacherSectionCard({ section, isOpen }) {
  return (
    <Card variant="outlined" sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <CardContent sx={{ flex: 1 }}>
        <Stack direction="row" sx={{ justifyContent: 'space-between', mb: 1.5 }}>
          <Chip
            size="small"
            label={`${formatPeriod(section)} · ${isOpen ? 'Open' : 'Closed'}`}
            color={isOpen ? 'primary' : 'default'}
            variant={isOpen ? 'filled' : 'outlined'}
          />
          <Stack direction="row" spacing={0.5} sx={{ alignItems: 'center', color: 'text.secondary' }}>
            <GroupsOutlinedIcon fontSize="small" />
            <Typography variant="body2">Capacity {section.sectionCapacity}</Typography>
          </Stack>
        </Stack>
        <Typography variant="caption" sx={{ fontFamily: 'monospace', fontWeight: 700, color: 'primary.main' }}>
          {section.courseCode}
        </Typography>
        <Typography variant="h6" sx={{ lineHeight: 1.3, mb: 2 }}>
          {section.courseName}
        </Typography>
        <ScheduleChips blocks={section.scheduleBlocks} />
      </CardContent>
      <CardActions sx={{ px: 2, pb: 2 }}>
        <Button component={RouterLink} to={`/teacher/sections/${section.sectionId}`} variant="contained" fullWidth>
          Students and grades
        </Button>
      </CardActions>
    </Card>
  )
}

export default TeacherSectionCard
