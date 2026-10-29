import Chip from '@mui/material/Chip'
import Stack from '@mui/material/Stack'
import Tooltip from '@mui/material/Tooltip'
import { DAYS, MODULES } from '../constants/schedule'

function ScheduleChips({ blocks = [] }) {
  return (
    <Stack direction="row" sx={{ flexWrap: 'wrap', gap: 0.5 }}>
      {blocks.map((block) => {
        const day = DAYS.find((item) => item.key === block[0])
        const module = MODULES.find((item) => item.number === Number(block.slice(1)))

        return (
          <Tooltip key={block} title={`${day?.label} ${module?.time}`}>
            <Chip label={block} size="small" variant="outlined" color="primary" sx={{ fontFamily: 'monospace' }} />
          </Tooltip>
        )
      })}
    </Stack>
  )
}

export default ScheduleChips