import Typography from '@mui/material/Typography'
import useAuth from '../auth/useAuth'

function HomePage() {
  const { me } = useAuth()

  return (
    <>
      <Typography variant="h4" gutterBottom>
        Welcome, {me.fullName}
      </Typography>
      <Typography color="text.secondary">Role: {me.roles.join(', ')}</Typography>
    </>
  )
}

export default HomePage
