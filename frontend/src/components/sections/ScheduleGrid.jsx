import Box from '@mui/material/Box'
import Checkbox from '@mui/material/Checkbox'
import Chip from '@mui/material/Chip'
import Stack from '@mui/material/Stack'
import Table from '@mui/material/Table'
import TableBody from '@mui/material/TableBody'
import TableCell from '@mui/material/TableCell'
import TableContainer from '@mui/material/TableContainer'
import TableHead from '@mui/material/TableHead'
import TableRow from '@mui/material/TableRow'
import Typography from '@mui/material/Typography'
import { DAYS, MODULES } from '../../constants/schedule'

const FIRST_AFTERNOON_MODULE = 4

function blockCountColor(selected, required) {
  if (required === null) return 'default'
  if (selected === required) return 'success'
  return selected > required ? 'error' : 'warning'
}

function ScheduleGrid({ selected, required, onToggle }) {
  const requiredLabel = required === null ? '-' : required

  return (
    <>
      <Stack direction="row" sx={{ justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Box>
          <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
            Weekly schedule
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Required blocks = course TEL hours / 2
          </Typography>
        </Box>
        <Chip
          label={`${selected.length} selected of ${requiredLabel} required`}
          color={blockCountColor(selected.length, required)}
        />
      </Stack>
      <TableContainer sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 2 }}>
        <Table size="small" sx={{ '& td, & th': { textAlign: 'center' } }}>
          <TableHead>
            <TableRow>
              <TableCell sx={{ textAlign: 'left !important' }}>Module</TableCell>
              {DAYS.map((day) => (
                <TableCell key={day.key}>
                  {day.key}
                  <Typography component="div" variant="caption" color="text.secondary">
                    {day.label}
                  </Typography>
                </TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {MODULES.map((module) => (
              <TableRow
                key={module.number}
                sx={
                  module.number === FIRST_AFTERNOON_MODULE
                    ? { '& td': { borderTop: '2px dashed', borderTopColor: 'divider' } }
                    : undefined
                }
              >
                <TableCell sx={{ textAlign: 'left !important', whiteSpace: 'nowrap' }}>
                  <Typography variant="body2" sx={{ fontWeight: 700 }}>
                    M{module.number}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {module.time}
                  </Typography>
                </TableCell>
                {DAYS.map((day) => {
                  const block = `${day.key}${module.number}`
                  const checked = selected.includes(block)

                  return (
                    <TableCell key={block} sx={{ bgcolor: checked ? 'action.selected' : undefined }}>
                      <Checkbox
                        checked={checked}
                        onChange={() => onToggle(block)}
                        slotProps={{ input: { 'aria-label': block } }}
                      />
                    </TableCell>
                  )
                })}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </>
  )
}

export default ScheduleGrid
