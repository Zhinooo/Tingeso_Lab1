import { Link as RouterLink } from 'react-router-dom'
import Button from '@mui/material/Button'
import AddIcon from '@mui/icons-material/Add'

function NewButton({ to, children }) {
  return (
    <Button component={RouterLink} to={to} variant="contained" startIcon={<AddIcon />}>
      {children}
    </Button>
  )
}

export default NewButton