import { Link as RouterLink } from 'react-router-dom'
import Button from '@mui/material/Button'

function RowAction({ label, onClick, to, color = 'primary', disabled }) {
  if (to) {
    return (
      <Button component={RouterLink} to={to} size="small" variant="contained" color={color} disableElevation>
        {label}
      </Button>
    )
  }

  return (
    <Button onClick={onClick} size="small" variant="contained" color={color} disabled={disabled} disableElevation>
      {label}
    </Button>
  )
}

export default RowAction
