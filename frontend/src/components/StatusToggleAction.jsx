import RowAction from './RowAction'

function StatusToggleAction({ active, onClick }) {
  return (
    <RowAction
      label={active ? 'Desactivar' : 'Activar'}
      color={active ? 'inherit' : 'success'}
      onClick={onClick}
    />
  )
}

export default StatusToggleAction
