import Box from '@mui/material/Box'
import Checkbox from '@mui/material/Checkbox'
import Chip from '@mui/material/Chip'
import FormControlLabel from '@mui/material/FormControlLabel'
import Grid from '@mui/material/Grid'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import { MAX_PREREQUISITES } from '../../constants/courses'

function PrerequisitePicker({ options, selected, hasStudyplan, onToggle }) {
  const isFull = selected.length >= MAX_PREREQUISITES

  return (
    <>
      <Stack direction="row" sx={{ alignItems: 'center', justifyContent: 'space-between', mb: 1 }}>
        <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
          Prerequisites
        </Typography>
        <Chip
          size="small"
          label={`${selected.length} of ${MAX_PREREQUISITES} max`}
          color={isFull ? 'warning' : 'default'}
        />
      </Stack>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 1.5 }}>
        Only courses from earlier semesters of the same plan can be prerequisites.
      </Typography>
      {options.length === 0 ? (
        <Box
          sx={{
            p: 2,
            borderRadius: 2,
            border: '1px dashed',
            borderColor: 'divider',
            color: 'text.secondary',
            fontSize: 14,
          }}
        >
          {hasStudyplan ? 'No earlier-semester courses available.' : 'Select a study plan first.'}
        </Box>
      ) : (
        <Grid container spacing={0.5}>
          {options.map((course) => {
            const checked = selected.includes(course.courseCode)

            return (
              <Grid key={course.courseId} size={6}>
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={checked}
                      onChange={() => onToggle(course.courseCode)}
                      disabled={!checked && isFull}
                    />
                  }
                  label={
                    <Typography variant="body2">
                      <b>{course.courseCode}</b> · {course.courseName}{' '}
                      <Typography component="span" variant="caption" color="text.secondary">
                        (S{course.semester})
                      </Typography>
                    </Typography>
                  }
                />
              </Grid>
            )
          })}
        </Grid>
      )}
    </>
  )
}

export default PrerequisitePicker
