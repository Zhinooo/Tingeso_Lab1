import Stack from '@mui/material/Stack'
import { STATUS_LABEL } from '../../constants/statuses'
import { STUDENT_STATUSES } from '../../constants/students'
import SearchField from '../SearchField'
import SelectFilter from '../SelectFilter'

const STATUS_OPTIONS = STUDENT_STATUSES.map((status) => ({ value: status, label: STATUS_LABEL[status] }))

function StudentFilters({ search, status, careerCode, careerCodes, onSearchChange, onStatusChange, onCareerChange }) {
  return (
    <Stack direction="row" spacing={2} sx={{ mb: 2 }}>
      <SearchField value={search} placeholder="Buscar por RUN, nombre o correo" onChange={onSearchChange} />
      <SelectFilter label="Estado" value={status} options={STATUS_OPTIONS} onChange={onStatusChange} />
      <SelectFilter
        label="Carrera"
        value={careerCode}
        options={careerCodes.map((code) => ({ value: code, label: code }))}
        allLabel="Todas"
        onChange={onCareerChange}
      />
    </Stack>
  )
}

export default StudentFilters
