import MenuItem from '@mui/material/MenuItem'
import Select from '@mui/material/Select'
import { MANUAL_STUDENT_STATUSES } from '../../constants/students'
import StatusChip from '../StatusChip'

function StudentStatusSelect({ status, onChange }) {
  if (!MANUAL_STUDENT_STATUSES.includes(status)) {
    return <StatusChip status={status} />
  }

  return (
    <Select
      size="small"
      value={status}
      onChange={(event) => onChange(event.target.value)}
      renderValue={(value) => <StatusChip status={value} />}
      sx={{ minWidth: 190, '& .MuiSelect-select': { py: 0.75 } }}
    >
      {MANUAL_STUDENT_STATUSES.map((option) => (
        <MenuItem key={option} value={option}>
          <StatusChip status={option} />
        </MenuItem>
      ))}
    </Select>
  )
}

export default StudentStatusSelect
