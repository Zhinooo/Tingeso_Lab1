import Paper from '@mui/material/Paper'
import Table from '@mui/material/Table'
import TableBody from '@mui/material/TableBody'
import TableCell from '@mui/material/TableCell'
import TableContainer from '@mui/material/TableContainer'
import TableHead from '@mui/material/TableHead'
import TableRow from '@mui/material/TableRow'
import Typography from '@mui/material/Typography'
import { DAYS, MODULES } from '../../constants/schedule'

function WeeklySchedule({ entries }) {
  const entryByBlock = {}
  entries.forEach((entry) => entry.blocks.forEach((block) => (entryByBlock[block] = entry)))

  return (
    <TableContainer component={Paper}>
      <Table sx={{ tableLayout: 'fixed', '& td, & th': { textAlign: 'center' } }}>
        <TableHead>
          <TableRow>
            <TableCell sx={{ width: 130, textAlign: 'left !important' }}>Módulo</TableCell>
            {DAYS.map((day) => (
              <TableCell key={day.key}>{day.label}</TableCell>
            ))}
          </TableRow>
        </TableHead>
        <TableBody>
          {MODULES.map((module) => (
            <TableRow key={module.number}>
              <TableCell sx={{ textAlign: 'left !important', whiteSpace: 'nowrap' }}>
                <Typography variant="body2" sx={{ fontWeight: 700 }}>
                  M{module.number}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {module.time}
                </Typography>
              </TableCell>
              {DAYS.map((day) => {
                const entry = entryByBlock[`${day.key}${module.number}`]

                return (
                  <TableCell key={day.key} sx={{ bgcolor: entry ? 'action.selected' : undefined }}>
                    {entry && (
                      <>
                        <Typography variant="body2" sx={{ fontWeight: 700, fontFamily: 'monospace' }}>
                          {entry.courseCode}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          {entry.courseName}
                        </Typography>
                      </>
                    )}
                  </TableCell>
                )
              })}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  )
}

export default WeeklySchedule
