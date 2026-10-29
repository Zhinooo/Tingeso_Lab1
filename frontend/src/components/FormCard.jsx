import { Link as RouterLink } from 'react-router-dom'
import Button from '@mui/material/Button'
import Divider from '@mui/material/Divider'
import Paper from '@mui/material/Paper'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'

function FormCard({ title, description, onSubmit, cancelTo, submitLabel = 'Save', saving, children }) {
  return (
    <Paper component="form" onSubmit={onSubmit} noValidate sx={{ p: 4 }}>
      {title && (
        <Typography variant="h6" sx={{ mb: description ? 0.5 : 3 }}>
          {title}
        </Typography>
      )}
      {description && (
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          {description}
        </Typography>
      )}
      {children}
      <Divider sx={{ my: 3 }} />
      <Stack direction="row" spacing={1.5} sx={{ justifyContent: 'flex-end' }}>
        {cancelTo && (
          <Button component={RouterLink} to={cancelTo} color="inherit">
            Cancel
          </Button>
        )}
        <Button type="submit" variant="contained" disabled={saving}>
          {submitLabel}
        </Button>
      </Stack>
    </Paper>
  )
}

export default FormCard