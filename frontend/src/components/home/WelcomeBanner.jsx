import Box from '@mui/material/Box'
import Chip from '@mui/material/Chip'
import Typography from '@mui/material/Typography'

function WelcomeBanner({ fullName, username, roleLabel }) {
  const firstName = fullName.split(' ')[0]

  return (
    <Box
      sx={(theme) => ({
        mb: 4,
        p: 4,
        borderRadius: 3,
        color: 'common.white',
        background: `linear-gradient(120deg, ${theme.palette.sidebar.main} 0%, ${theme.palette.primary.main} 100%)`,
      })}
    >
      <Chip
        label={roleLabel}
        size="small"
        sx={{ bgcolor: 'rgba(255, 255, 255, 0.16)', color: 'common.white', mb: 1.5 }}
      />
      <Typography variant="h4" component="h1">
        Welcome, {firstName}
      </Typography>
      <Typography sx={{ opacity: 0.8, mt: 0.5 }}>
        {fullName} · Signed in as {username}
      </Typography>
    </Box>
  )
}

export default WelcomeBanner
