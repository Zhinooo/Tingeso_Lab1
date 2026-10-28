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
import HistoryEduOutlinedIcon from '@mui/icons-material/HistoryEduOutlined'

export const NAV = [
  {
    role: 'ADMIN',
    groups: [
      { title: 'Academic structure', items: [
        { label: 'Careers', to: '/admin/careers', icon: SchoolOutlinedIcon },
        { label: 'Study plans', to: '/admin/studyplans', icon: AccountTreeOutlinedIcon },
        { label: 'Courses', to: '/admin/courses', icon: MenuBookOutlinedIcon },
      ] },
      { title: 'People', items: [
        { label: 'Students', to: '/admin/students', icon: PeopleOutlineIcon },
        { label: 'Teachers', to: '/admin/teachers', icon: BadgeOutlinedIcon },
      ] },
      { title: 'Academic offer', items: [
        { label: 'Academic periods', to: '/admin/academic-periods', icon: EventOutlinedIcon },
        { label: 'Sections', to: '/admin/sections', icon: ViewWeekOutlinedIcon },
      ] },
      { title: 'Enrollment', items: [
        { label: 'Tuitions', to: '/admin/tuitions', icon: ReceiptLongOutlinedIcon },
        { label: 'Enrollments', to: '/admin/enrollments', icon: FactCheckOutlinedIcon },
      ] },
    ],
  },
  {
    role: 'TEACHER',
    groups: [{ title: 'Teaching', items: [{ label: 'My sections', to: '/teacher/sections', icon: ClassOutlinedIcon }] }],
  },
  {
    role: 'STUDENT',
    groups: [{ title: 'My studies', items: [
      { label: 'My profile', to: '/student/profile', icon: PersonOutlineIcon },
      { label: 'Enroll courses', to: '/student/enroll', icon: AddTaskIcon },
      { label: 'My enrollments', to: '/student/enrollments', icon: ListAltOutlinedIcon },
      { label: 'Academic history', to: '/student/history', icon: HistoryEduOutlinedIcon },
    ] }],
  },
]

export const ROLE_LABEL = { ADMIN: 'Administrator', TEACHER: 'Teacher', STUDENT: 'Student' }
