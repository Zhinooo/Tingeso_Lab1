import { useEffect, useState } from 'react'
import careerService from '../../services/career.service'

function CareerList() {
  const [careers, setCareers] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    careerService
      .getAll()
      .then((response) => setCareers(response.data))
      .catch((err) => setError(err.message))
  }, [])

  return (
    <div>
      {error && <p>{error}</p>}
      <pre>{JSON.stringify(careers, null, 2)}</pre>
    </div>
  )
}

export default CareerList