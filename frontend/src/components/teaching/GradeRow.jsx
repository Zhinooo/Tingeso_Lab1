import Button from '@mui/material/Button'
import TableCell from '@mui/material/TableCell'
import TableRow from '@mui/material/TableRow'
import TextField from '@mui/material/TextField'
import { MAX_GRADE, MIN_GRADE } from '../../constants/grades'
import { isValidGrade, resultOf } from '../../utils/grades'
import StatusChip from '../StatusChip'

function GradeRow({ enrollment, value, readOnly, saving, onChange, onSave }) {
  const invalid = value !== '' && !isValidGrade(value)
  const dirty = String(value) !== String(enrollment.finalGrade ?? '')

  return (
    <TableRow hover>
      <TableCell sx={{ fontFamily: 'monospace', whiteSpace: 'nowrap' }}>{enrollment.studentRun}</TableCell>
      <TableCell>
        <TextField
          type="number"
          size="small"
          value={value}
          onChange={(event) => onChange(enrollment.enrollmentId, event.target.value)}
          error={invalid}
          disabled={readOnly}
          placeholder="-"
          sx={{ width: 100 }}
          slotProps={{ htmlInput: { min: MIN_GRADE, max: MAX_GRADE, step: 0.1 } }}
        />
      </TableCell>
      <TableCell>
        <StatusChip status={resultOf(value)} />
      </TableCell>
      <TableCell align="right">
        <Button
          size="small"
          variant={dirty ? 'contained' : 'outlined'}
          onClick={() => onSave(enrollment)}
          disabled={readOnly || !isValidGrade(value) || !dirty || saving}
        >
          Save
        </Button>
      </TableCell>
    </TableRow>
  )
}

export default GradeRow
