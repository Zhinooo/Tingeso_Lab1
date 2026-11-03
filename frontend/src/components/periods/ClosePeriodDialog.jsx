import Alert from '@mui/material/Alert'
import Button from '@mui/material/Button'
import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogContentText from '@mui/material/DialogContentText'
import DialogTitle from '@mui/material/DialogTitle'
import LinearProgress from '@mui/material/LinearProgress'
import List from '@mui/material/List'
import ListItem from '@mui/material/ListItem'
import ListItemText from '@mui/material/ListItemText'
import { formatPeriod } from '../../utils/periods'

function ClosePeriodSummary({ summary }) {
  if (!summary) {
    return (
      <>
        <DialogContentText sx={{ mb: 2 }}>Revisando las notas del período...</DialogContentText>
        <LinearProgress />
      </>
    )
  }

  if (summary.pendingCount > 0) {
    return (
      <>
        <Alert severity="warning" sx={{ mb: 1 }}>
          Aún hay {summary.pendingCount} de {summary.total} inscripciones sin nota final. No se puede cerrar hasta que los
          docentes registren todas.
        </Alert>
        <List dense>
          {summary.pending.map((entry) => (
            <ListItem key={entry.courseCode} disableGutters>
              <ListItemText
                primary={`${entry.courseCode} · ${entry.courseName}`}
                secondary={`${entry.count} ${entry.count === 1 ? 'nota pendiente' : 'notas pendientes'}`}
              />
            </ListItem>
          ))}
        </List>
      </>
    )
  }

  return (
    <DialogContentText>
      {summary.total === 0
        ? 'Este período no tiene inscripciones, así que al cerrarlo no cambiará el estado de ningún estudiante.'
        : `Las ${summary.total} inscripciones tienen nota final. Al cerrar, pasarán al historial académico y se actualizará el estado de los estudiantes.`}{' '}
      Esta acción es irreversible.
    </DialogContentText>
  )
}

function ClosePeriodDialog({ period, summary, onConfirm, onClose }) {
  const canClose = Boolean(summary) && summary.pendingCount === 0

  return (
    <Dialog open={Boolean(period)} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle sx={{ fontWeight: 700 }}>{period && `¿Cerrar el período ${formatPeriod(period)}?`}</DialogTitle>
      <DialogContent>
        <ClosePeriodSummary summary={summary} />
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} color="inherit">
          {canClose || !summary ? 'Cancelar' : 'Entendido'}
        </Button>
        <Button onClick={onConfirm} variant="contained" color="error" disabled={!canClose}>
          Cerrar período
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default ClosePeriodDialog
