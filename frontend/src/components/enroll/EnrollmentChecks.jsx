import Stack from '@mui/material/Stack'
import ValidationRule from '../ValidationRule'

function EnrollmentChecks({ checks }) {
  return (
    <Stack spacing={0.5}>
      {checks.map((check) => (
        <ValidationRule key={check.text} ok={check.ok}>
          {check.text}
        </ValidationRule>
      ))}
    </Stack>
  )
}

export default EnrollmentChecks
