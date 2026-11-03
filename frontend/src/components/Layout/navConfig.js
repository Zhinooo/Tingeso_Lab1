import SchoolOutlinedIcon from '@mui/icons-material/SchoolOutlined'
import AccountTreeOutlinedIcon from '@mui/icons-material/AccountTreeOutlined'
import MenuBookOutlinedIcon from '@mui/icons-material/MenuBookOutlined'
import PeopleOutlineIcon from '@mui/icons-material/PeopleOutlineOutlined'
import BadgeOutlinedIcon from '@mui/icons-material/BadgeOutlined'
import EventOutlinedIcon from '@mui/icons-material/EventOutlined'
import ViewWeekOutlinedIcon from '@mui/icons-material/ViewWeekOutlined'
import ReceiptLongOutlinedIcon from '@mui/icons-material/ReceiptLongOutlined'
import FactCheckOutlinedIcon from '@mui/icons-material/FactCheckOutlined'
import ClassOutlinedIcon from '@mui/icons-material/ClassOutlined'
import PersonOutlineIcon from '@mui/icons-material/PersonOutlineOutlined'
import AddTaskIcon from '@mui/icons-material/AddTask'
import ListAltOutlinedIcon from '@mui/icons-material/ListAltOutlined'
import CalendarMonthOutlinedIcon from '@mui/icons-material/CalendarMonthOutlined'
import HistoryEduOutlinedIcon from '@mui/icons-material/HistoryEduOutlined'

export const NAV = [
  {
    role: 'ADMIN',
    items: [
      { label: 'Carreras', to: '/admin/careers', icon: SchoolOutlinedIcon },
      { label: 'Planes de estudio', to: '/admin/studyplans', icon: AccountTreeOutlinedIcon },
      { label: 'Ramos', to: '/admin/courses', icon: MenuBookOutlinedIcon },
      { label: 'Estudiantes', to: '/admin/students', icon: PeopleOutlineIcon },
      { label: 'Docentes', to: '/admin/teachers', icon: BadgeOutlinedIcon },
      { label: 'Períodos académicos', to: '/admin/academic-periods', icon: EventOutlinedIcon },
      { label: 'Secciones', to: '/admin/sections', icon: ViewWeekOutlinedIcon },
      { label: 'Matrículas', to: '/admin/tuitions', icon: ReceiptLongOutlinedIcon },
      { label: 'Inscripciones', to: '/admin/enrollments', icon: FactCheckOutlinedIcon },
    ],
  },
  {
    role: 'TEACHER',
    items: [
      { label: 'Mis secciones', to: '/teacher/sections', icon: ClassOutlinedIcon },
    ],
  },
  {
    role: 'STUDENT',
    items: [
      { label: 'Mi perfil', to: '/student/profile', icon: PersonOutlineIcon },
      { label: 'Inscribir ramos', to: '/student/enroll', icon: AddTaskIcon },
      { label: 'Mis inscripciones', to: '/student/enrollments', icon: ListAltOutlinedIcon },
      { label: 'Mi horario', to: '/student/schedule', icon: CalendarMonthOutlinedIcon },
      { label: 'Historial académico', to: '/student/history', icon: HistoryEduOutlinedIcon },
    ],
  },
]
