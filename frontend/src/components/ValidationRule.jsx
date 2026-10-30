import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import ErrorOutlineIcon from '@mui/icons-material/ErrorOutlineOutlined'

function ValidationRule({ ok, children }) {
  return (
    <Stack direction="row" spacing={1} sx={{ alignItems: 'center', color: ok ? 'success.main' : 'error.main' }}>
      {ok ? <CheckCircleIcon fontSize="small" /> : <ErrorOutlineIcon fontSize="small" />}
      <Typography variant="body2">{children}</Typography>
    </Stack>
  )
}

export default ValidationRule
