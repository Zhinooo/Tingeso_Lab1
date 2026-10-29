import LinearProgress from '@mui/material/LinearProgress'
import Paper from '@mui/material/Paper'
import Table from '@mui/material/Table'
import TableBody from '@mui/material/TableBody'
import TableCell from '@mui/material/TableCell'
import TableContainer from '@mui/material/TableContainer'
import TableHead from '@mui/material/TableHead'
import TableRow from '@mui/material/TableRow'
import EmptyState from './EmptyState'

function DataTable({ columns, rowCount, loading, emptyMessage, emptyHint, emptyAction, children }) {
  return (
    <Paper sx={{ overflow: 'hidden' }}>
      {loading && <LinearProgress />}
      <TableContainer>
        <Table sx={{ '& tbody tr:last-of-type td': { borderBottom: 0 } }}>
          <TableHead>
            <TableRow>
              {columns.map((column) => (
                <TableCell key={column.label} align={column.align}>
                  {column.label}
                </TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>{children}</TableBody>
        </Table>
      </TableContainer>
      {!loading && rowCount === 0 && (
        <EmptyState message={emptyMessage} hint={emptyHint} action={emptyAction} />
      )}
    </Paper>
  )
}

export default DataTable