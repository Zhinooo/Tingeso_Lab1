import { createTheme } from '@mui/material/styles'

const theme = createTheme({
  palette: {
    primary: { main: '#1565c0' },
    background: { default: '#f5f7fb' },
    sidebar: {
      main: '#0f2747',
      text: '#c9d6ea',
      muted: '#8ea3c4',
      hover: 'rgba(255, 255, 255, 0.06)',
    },
  },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        'input[type=number]': { MozAppearance: 'textfield' },
        'input[type=number]::-webkit-outer-spin-button, input[type=number]::-webkit-inner-spin-button': {
          WebkitAppearance: 'none',
          margin: 0,
        },
      },
    },
  },
})

export default theme
