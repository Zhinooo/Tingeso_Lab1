import ToggleOffOutlinedIcon from '@mui/icons-material/ToggleOffOutlined'
import ToggleOnOutlinedIcon from '@mui/icons-material/ToggleOnOutlined'
import RowAction from './RowAction'

function StatusToggleAction({ active, onClick }) {
  return (
    <RowAction
      title={active ? 'Deactivate' : 'Activate'}
      icon={active ? <ToggleOnOutlinedIcon fontSize="small" /> : <ToggleOffOutlinedIcon fontSize="small" />}
      color={active ? 'success' : 'default'}
      onClick={onClick}
    />
  )
}

export default StatusToggleAction