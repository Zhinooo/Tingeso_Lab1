import Checkbox from '@mui/material/Checkbox'
import FormControlLabel from '@mui/material/FormControlLabel'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import DataTable from '../DataTable'
import CareerStudentRow from './CareerStudentRow'

const COLUMNS = [
  { label: 'RUN' },
  { label: 'Nombre completo' },
  { label: 'Correo' },
  { label: 'Plan de estudio' },
  { label: 'Estado' },
]

function CareerStudentTable({ students, totalCount, includeFinished, onIncludeFinishedChange }) {
  const hasHiddenStudents = totalCount > 0 && students.length === 0

  return (
    <>
      <Stack direction="row" sx={{ justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h6">Estudiantes de la carrera ({students.length})</Typography>
        <FormControlLabel
          control={
            <Checkbox checked={includeFinished} onChange={(event) => onIncludeFinishedChange(event.target.checked)} />
          }
          label="Incluir egresados y expulsados"
        />
      </Stack>
      <DataTable
        columns={COLUMNS}
        rowCount={students.length}
        emptyMessage={hasHiddenStudents ? 'No hay estudiantes activos' : 'Esta carrera aún no tiene estudiantes'}
        emptyHint={hasHiddenStudents ? 'Marca «Incluir egresados y expulsados» para ver el resto.' : undefined}
      >
        {students.map((student) => (
          <CareerStudentRow key={student.studentId} student={student} />
        ))}
      </DataTable>
    </>
  )
}

export default CareerStudentTable
