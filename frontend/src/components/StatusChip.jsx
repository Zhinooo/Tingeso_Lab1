import Chip from '@mui/material/Chip'

const COLORS = {
  ACTIVE: 'success',
  INACTIVE: 'default',
  CURRENT: 'success',
  NOT_CURRENT: 'default',
  OPEN: 'success',
  CLOSED: 'default',
  REGULAR: 'success',
  DEFERRED: 'warning',
  TEMPORARY_WITHDRAWAL: 'warning',
  GRADUATED: 'primary',
  EXPELLED: 'error',
  APPROVED: 'success',
  FAILED: 'error',
}

function StatusChip({ status, size = 'small' }) {
  if (!status) return null

  const color = COLORS[status] ?? 'default'
  const label = status
    .replaceAll('_', ' ')
    .toLowerCase()
    .replace(/^./, (letter) => letter.toUpperCase())

  return (
    <Chip
      size={size}
      label={label}
      color={color}
      variant={color === 'default' ? 'outlined' : 'filled'}
      sx={{ minWidth: 72 }}
    />
  )
}

export default StatusChip