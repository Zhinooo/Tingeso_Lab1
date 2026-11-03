import Stack from '@mui/material/Stack'
import { STATUS_LABEL } from '../../constants/statuses'
import SearchField from '../SearchField'
import SelectFilter from '../SelectFilter'

const STATUS_OPTIONS = ['ACTIVE', 'INACTIVE'].map((status) => ({ value: status, label: STATUS_LABEL[status] }))

function TeacherFilters({ search, status, onSearchChange, onStatusChange }) {
  return (
    <Stack direction="row" spacing={2} sx={{ mb: 2 }}>
      <SearchField value={search} placeholder="Buscar por RUN, nombre o correo" onChange={onSearchChange} />
      <SelectFilter label="Estado" value={status} options={STATUS_OPTIONS} onChange={onStatusChange} />
    </Stack>
  )
}

export default TeacherFilters
