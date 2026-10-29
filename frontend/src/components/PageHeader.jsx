import { Link as RouterLink } from 'react-router-dom'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import ArrowBackIcon from '@mui/icons-material/ArrowBack'

function PageHeader({ title, subtitle, backTo, action }) {
  return (
    <Box sx={{ mb: 3 }}>
      {backTo && (
        <Button
          component={RouterLink}
          to={backTo}
          startIcon={<ArrowBackIcon />}
          size="small"
          sx={{ mb: 1, ml: -1 }}
        >
          Back
        </Button>
      )}
      <Stack direction="row" spacing={2} sx={{ justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography variant="h5" component="h1">
            {title}
          </Typography>
          {subtitle && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
              {subtitle}
            </Typography>
          )}
        </Box>
        {action}
      </Stack>
    </Box>
  )
}

export default PageHeader