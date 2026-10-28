import { useEffect, useState } from 'react'
import meService from '../services/me.service'

function HomePage() {
  const [me, setMe] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    meService
      .getMe()
      .then((response) => setMe(response.data))
      .catch((err) => setError(err.message))
  }, [])

  return (
    <div>
      {error && <p>{error}</p>}
      <pre>{JSON.stringify(me, null, 2)}</pre>
    </div>
  )
}

export default HomePage