import { Link as RouterLink } from 'react-router-dom'
import IconButton from '@mui/material/IconButton'
import Tooltip from '@mui/material/Tooltip'

function RowAction({ title, icon, onClick, to, color = 'default', disabled }) {
  const button = to ? (
    <IconButton component={RouterLink} to={to} size="small" color={color} aria-label={title}>
      {icon}
    </IconButton>
  ) : (
    <IconButton onClick={onClick} size="small" color={color} aria-label={title} disabled={disabled}>
      {icon}
    </IconButton>
  )

  return (
    <Tooltip title={title}>
      <span>{button}</span>
    </Tooltip>
  )
}

export default RowAction