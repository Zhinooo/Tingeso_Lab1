import Box from '@mui/material/Box'
import { SEMESTER_COLORS } from '../../constants/semesters'

function SemesterBadge({ semester }) {
  const color = SEMESTER_COLORS[semester]

  return (
    <Box
      sx={{
        width: 28,
        height: 28,
        borderRadius: '50%',
        bgcolor: `${color}1a`,
        color,
        display: 'grid',
        placeItems: 'center',
        fontWeight: 700,
        fontSize: 13,
      }}
    >
      {semester}
    </Box>
  )
}

export default SemesterBadge
