import Alert from '@mui/material/Alert'
import AlertTitle from '@mui/material/AlertTitle'

function ErrorAlert({ message, title, onClose }) {
  if (!message) return null

  return (
    <Alert severity="error" onClose={onClose} sx={{ mb: 2 }}>
      {title && <AlertTitle sx={{ fontWeight: 700 }}>{title}</AlertTitle>}
      {message}
    </Alert>
  )
}

export default ErrorAlert
