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
        { label: 'Careers', to: '/admin/careers', description: 'Programs offered by the institute', icon: SchoolOutlinedIcon },
        { label: 'Study plans', to: '/admin/studyplans', description: 'Plans of each career and the current one', icon: AccountTreeOutlinedIcon },
        { label: 'Courses', to: '/admin/courses', description: 'Courses, workload and prerequisites', icon: MenuBookOutlinedIcon },
      ] },
      { title: 'People', items: [
        { label: 'Students', to: '/admin/students', description: 'Student records and academic status', icon: PeopleOutlineIcon },
        { label: 'Teachers', to: '/admin/teachers', description: 'Faculty and their login accounts', icon: BadgeOutlinedIcon },
      ] },
      { title: 'Academic offer', items: [
        { label: 'Academic periods', to: '/admin/academic-periods', description: 'Open and close semesters', icon: EventOutlinedIcon },
        { label: 'Sections', to: '/admin/sections', description: 'Course offer, teachers and schedules', icon: ViewWeekOutlinedIcon },
      ] },
      { title: 'Enrollment', items: [
        { label: 'Tuitions', to: '/admin/tuitions', description: 'Register students in a period', icon: ReceiptLongOutlinedIcon },
        { label: 'Enrollments', to: '/admin/enrollments', description: 'Course enrollments and final grades', icon: FactCheckOutlinedIcon },
      ] },
    ],
  },
  {
    role: 'TEACHER',
    groups: [{ title: 'Teaching', items: [{ label: 'My sections', to: '/teacher/sections', description: 'Your sections and final grades', icon: ClassOutlinedIcon }] }],
  },
  {
    role: 'STUDENT',
    groups: [{ title: 'My studies', items: [
      { label: 'My profile', to: '/student/profile', description: 'Your data and your study plan', icon: PersonOutlineIcon },
      { label: 'Enroll courses', to: '/student/enroll', description: 'Pick your courses for an open period', icon: AddTaskIcon },
      { label: 'My enrollments', to: '/student/enrollments', description: 'Your current and past enrollments', icon: ListAltOutlinedIcon },
      { label: 'Academic history', to: '/student/history', description: 'Your final grades and average', icon: HistoryEduOutlinedIcon },
    ] }],
  },
]

export const ROLE_LABEL = { ADMIN: 'Administrator', TEACHER: 'Teacher', STUDENT: 'Student' }
