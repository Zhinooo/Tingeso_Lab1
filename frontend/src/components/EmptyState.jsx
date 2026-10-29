import Box from '@mui/material/Box'
import Typography from '@mui/material/Typography'
import InboxOutlinedIcon from '@mui/icons-material/InboxOutlined'

function EmptyState({ message, hint, action }) {
  return (
    <Box sx={{ py: 7, px: 2, textAlign: 'center', color: 'text.secondary' }}>
      <InboxOutlinedIcon sx={{ fontSize: 44, color: 'grey.400', mb: 1 }} />
      <Typography variant="subtitle1" color="text.primary" sx={{ fontWeight: 600 }}>
        {message}
      </Typography>
      {hint && (
        <Typography variant="body2" sx={{ mt: 0.5 }}>
          {hint}
        </Typography>
      )}
      {action && <Box sx={{ mt: 2 }}>{action}</Box>}
    </Box>
  )
}

export default EmptyState