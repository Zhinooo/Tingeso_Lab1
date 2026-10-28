import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import Layout from './components/Layout/Layout'
import ProtectedRoute from './auth/ProtectedRoute'
import HomePage from './pages/HomePage'
import AcademicPeriodList from './pages/admin/AcademicPeriodList'
import AddEditAcademicPeriod from './pages/admin/AddEditAcademicPeriod'
import AddEditCareer from './pages/admin/AddEditCareer'
import AddEditCourse from './pages/admin/AddEditCourse'
import AddEditSection from './pages/admin/AddEditSection'
import AddEditStudent from './pages/admin/AddEditStudent'
import AddEditStudyplan from './pages/admin/AddEditStudyplan'
import AddEditTeacher from './pages/admin/AddEditTeacher'
import AddTuition from './pages/admin/AddTuition'
import CareerList from './pages/admin/CareerList'
import CourseList from './pages/admin/CourseList'
import EnrollmentList from './pages/admin/EnrollmentList'
import SectionList from './pages/admin/SectionList'
import StudentHistory from './pages/admin/StudentHistory'
import StudentList from './pages/admin/StudentList'
import StudyplanList from './pages/admin/StudyplanList'
import TeacherList from './pages/admin/TeacherList'
import TuitionList from './pages/admin/TuitionList'
import EnrollCourses from './pages/student/EnrollCourses'
import MyEnrollments from './pages/student/MyEnrollments'
import MyHistory from './pages/student/MyHistory'
import MyProfile from './pages/student/MyProfile'
import MySections from './pages/teacher/MySections'
import SectionGrades from './pages/teacher/SectionGrades'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<HomePage />} />

          <Route element={<ProtectedRoute roles={['ADMIN']} />}>
            <Route path="/admin/careers" element={<CareerList />} />
            <Route path="/admin/careers/new" element={<AddEditCareer />} />
            <Route path="/admin/careers/:id/edit" element={<AddEditCareer />} />

            <Route path="/admin/studyplans" element={<StudyplanList />} />
            <Route path="/admin/studyplans/new" element={<AddEditStudyplan />} />
            <Route path="/admin/studyplans/:id/edit" element={<AddEditStudyplan />} />

            <Route path="/admin/courses" element={<CourseList />} />
            <Route path="/admin/courses/new" element={<AddEditCourse />} />
            <Route path="/admin/courses/:id/edit" element={<AddEditCourse />} />

            <Route path="/admin/teachers" element={<TeacherList />} />
            <Route path="/admin/teachers/new" element={<AddEditTeacher />} />
            <Route path="/admin/teachers/:id/edit" element={<AddEditTeacher />} />

            <Route path="/admin/students" element={<StudentList />} />
            <Route path="/admin/students/new" element={<AddEditStudent />} />
            <Route path="/admin/students/:id/edit" element={<AddEditStudent />} />
            <Route path="/admin/students/:id/history" element={<StudentHistory />} />

            <Route path="/admin/academic-periods" element={<AcademicPeriodList />} />
            <Route path="/admin/academic-periods/new" element={<AddEditAcademicPeriod />} />
            <Route path="/admin/academic-periods/:id/edit" element={<AddEditAcademicPeriod />} />

            <Route path="/admin/sections" element={<SectionList />} />
            <Route path="/admin/sections/new" element={<AddEditSection />} />
            <Route path="/admin/sections/:id/edit" element={<AddEditSection />} />

            <Route path="/admin/tuitions" element={<TuitionList />} />
            <Route path="/admin/tuitions/new" element={<AddTuition />} />

            <Route path="/admin/enrollments" element={<EnrollmentList />} />
          </Route>

          <Route element={<ProtectedRoute roles={['TEACHER']} />}>
            <Route path="/teacher/sections" element={<MySections />} />
            <Route path="/teacher/sections/:id" element={<SectionGrades />} />
          </Route>

          <Route element={<ProtectedRoute roles={['STUDENT']} />}>
            <Route path="/student/profile" element={<MyProfile />} />
            <Route path="/student/enroll" element={<EnrollCourses />} />
            <Route path="/student/enrollments" element={<MyEnrollments />} />
            <Route path="/student/history" element={<MyHistory />} />
          </Route>
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
