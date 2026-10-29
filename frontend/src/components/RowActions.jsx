import Box from '@mui/material/Box'

function RowActions({ children }) {
  return <Box sx={{ display: 'inline-flex', gap: 0.5, whiteSpace: 'nowrap' }}>{children}</Box>
}

export default RowActions