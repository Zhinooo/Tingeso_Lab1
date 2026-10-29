import { Outlet } from 'react-router-dom'
import Box from '@mui/material/Box'
import Container from '@mui/material/Container'
import Toolbar from '@mui/material/Toolbar'
import Sidebar from './Sidebar'
import TopBar from './TopBar'

const SIDEBAR_WIDTH = 264

function Layout() {
  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'background.default' }}>
      <TopBar sidebarWidth={SIDEBAR_WIDTH} />
      <Sidebar width={SIDEBAR_WIDTH} />
      <Box component="main" sx={{ flex: 1, minWidth: 0 }}>
        <Toolbar />
        <Container maxWidth="xl" sx={{ py: 4, px: 4 }}>
          <Outlet />
        </Container>
      </Box>
    </Box>
  )
}

export default Layout