import { createTheme } from '@mui/material/styles'

const theme = createTheme({
  palette: {
    primary: { main: '#1565c0' },
    background: { default: '#f5f7fb' },
    sidebar: {
      main: '#0f2747',
      text: '#c9d6ea',
      muted: '#8ea3c4',
      subheader: '#7189ad',
      hover: 'rgba(255, 255, 255, 0.06)',
      divider: 'rgba(255, 255, 255, 0.08)',
    },
  },
})

export default theme
