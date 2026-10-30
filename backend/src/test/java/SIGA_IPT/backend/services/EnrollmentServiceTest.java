package SIGA_IPT.backend.services;

import SIGA_IPT.backend.dtos.EnrollmentDTOs.AvailableCourseDTO;
import SIGA_IPT.backend.dtos.EnrollmentDTOs.EnrollmentRequestDTO;
import SIGA_IPT.backend.dtos.EnrollmentDTOs.EnrollmentResponseDTO;
import SIGA_IPT.backend.entities.AcademicPeriodEntity;
import SIGA_IPT.backend.entities.CourseEntity;
import SIGA_IPT.backend.entities.EnrollmentEntity;
import SIGA_IPT.backend.entities.SectionEntity;
import SIGA_IPT.backend.entities.SectionEntity.ScheduleDay;
import SIGA_IPT.backend.entities.StudentEntity;
import SIGA_IPT.backend.entities.StudyplanEntity;
import SIGA_IPT.backend.entities.TeacherEntity;
import SIGA_IPT.backend.exceptions.ForbiddenOperationException;
import SIGA_IPT.backend.mappers.EnrollmentMapper;
import SIGA_IPT.backend.repositories.AcademicPeriodRepository;
import SIGA_IPT.backend.repositories.CourseRepository;
import SIGA_IPT.backend.repositories.EnrollmentRepository;
import SIGA_IPT.backend.repositories.SectionRepository;
import SIGA_IPT.backend.repositories.StudentRepository;
import SIGA_IPT.backend.repositories.TuitionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private AcademicPeriodRepository academicPeriodRepository;
    @Mock
    private TuitionRepository tuitionRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private SectionRepository sectionRepository;

    private EnrollmentService service;
    private StudentEntity student;
    private AcademicPeriodEntity period;
    private StudyplanEntity plan;
    private TeacherEntity teacher;
    private List<CourseEntity> courses;
    private List<SectionEntity> sections;

    @BeforeEach
    void setUp(){
        service = new EnrollmentService(enrollmentRepository, studentRepository, academicPeriodRepository, tuitionRepository,
                courseRepository, sectionRepository, new EnrollmentMapper());

        plan = new StudyplanEntity();
        plan.setStudyplanId(1L);
        student = new StudentEntity();
        student.setStudentId(1L);
        student.setStudentRun("1-9");
        student.setStudentStatus(StudentEntity.StudentStatus.REGULAR);
        student.setStudyplan(plan);
        period = period(1L, AcademicPeriodEntity.AcademicPeriodStatus.OPEN);
        teacher = new TeacherEntity();
        teacher.setTeacherId(1L);
        teacher.setTeacherFullname("Teacher One");

        courses = new ArrayList<>();
        for (int i = 1; i <= 7; i++){
            courses.add(course((long) i, "A" + i, 1));
        }
        CourseEntity b1 = course(8L, "B1", 2);
        b1.getPrerequisites().add(courses.get(0));
        courses.add(b1);
        sections = new ArrayList<>();
        for (CourseEntity course : courses){
            sections.add(section(10L + course.getCourseId(), course, course.getCourseId().intValue()));
        }

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(academicPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(tuitionRepository.existsByStudentAndAcademicPeriod(student, period)).thenReturn(true);
        when(courseRepository.findByStudyplan(plan)).thenReturn(courses);
        when(sectionRepository.findByAcademicPeriod(period)).thenReturn(sections);
        when(enrollmentRepository.findByStudentAndAcademicPeriod(student, period)).thenReturn(new ArrayList<>());
        when(enrollmentRepository.findByStudentAndAcademicPeriod_AcademicPeriodStatus(student,
                AcademicPeriodEntity.AcademicPeriodStatus.CLOSED)).thenReturn(new ArrayList<>());
        when(enrollmentRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        when(sectionRepository.findForUpdateBySectionId(any())).thenAnswer(inv -> Optional.empty());
    }

    private AcademicPeriodEntity period(Long id, AcademicPeriodEntity.AcademicPeriodStatus status){
        AcademicPeriodEntity period = new AcademicPeriodEntity();
        period.setAcademicPeriodId(id);
        period.setAcademicYear(2030);
        period.setAcademicSemester(1);
        period.setAcademicPeriodStatus(status);
        return period;
    }

    private CourseEntity course(Long id, String code, int semester){
        CourseEntity course = new CourseEntity();
        course.setCourseId(id);
        course.setCourseCode(code);
        course.setCourseName("Name " + code);
        course.setSemester(semester);
        course.setStudyplan(plan);
        course.setPrerequisites(new ArrayList<>());
        return course;
    }

    private SectionEntity section(Long id, CourseEntity course, int blockIndex){
        ScheduleDay[] days = ScheduleDay.values();
        SectionEntity section = new SectionEntity();
        section.setSectionId(id);
        section.setCourse(course);
        section.setAcademicPeriod(period);
        section.setTeacher(teacher);
        section.setSectionCapacity(30);
        section.setScheduleBlocks(new ArrayList<>(List.of(
                new SectionEntity.ScheduleBlock(days[blockIndex % days.length], 1 + blockIndex / days.length))));
        return section;
    }

    private EnrollmentEntity enrollment(Long id, CourseEntity course, SectionEntity section, AcademicPeriodEntity period, Double grade){
        EnrollmentEntity enrollment = new EnrollmentEntity();
        enrollment.setEnrollmentId(id);
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setSection(section);
        enrollment.setAcademicPeriod(period);
        enrollment.setEnrollmentDate(LocalDate.now());
        enrollment.setFinalGrade(grade);
        return enrollment;
    }

    private SectionEntity sectionOf(String courseCode){
        return sections.stream().filter(s -> s.getCourse().getCourseCode().equals(courseCode)).findFirst().orElseThrow();
    }

    private CourseEntity courseOf(String courseCode){
        return courses.stream().filter(c -> c.getCourseCode().equals(courseCode)).findFirst().orElseThrow();
    }

    private void givenApprovedInClosedPeriod(String courseCode){
        AcademicPeriodEntity closed = period(9L, AcademicPeriodEntity.AcademicPeriodStatus.CLOSED);
        EnrollmentEntity approved = enrollment(90L, courseOf(courseCode), sectionOf(courseCode), closed, 5.5);
        when(enrollmentRepository.findByStudentAndAcademicPeriod_AcademicPeriodStatus(student,
                AcademicPeriodEntity.AcademicPeriodStatus.CLOSED)).thenReturn(List.of(approved));
    }

    private EnrollmentRequestDTO request(Long... courseIds){
        return new EnrollmentRequestDTO(1L, new ArrayList<>(List.of(courseIds)));
    }

    @Test
    void createEnrollsTheStudentInThreeCourses(){
        List<EnrollmentResponseDTO> result = service.create(1L, request(1L, 2L, 3L));

        assertThat(result).hasSize(3);
        assertThat(result).extracting(EnrollmentResponseDTO::courseCode).containsExactly("A1", "A2", "A3");
        assertThat(result).allSatisfy(e -> {
            assertThat(e.studentId()).isEqualTo(1L);
            assertThat(e.enrollmentDate()).isEqualTo(LocalDate.now());
            assertThat(e.finalGrade()).isNull();
        });
    }

    @Test
    void createLocksTheSelectedSectionsInIdOrder(){
        service.create(1L, request(3L, 1L, 2L));

        org.mockito.InOrder order = org.mockito.Mockito.inOrder(sectionRepository);
        order.verify(sectionRepository).findForUpdateBySectionId(11L);
        order.verify(sectionRepository).findForUpdateBySectionId(12L);
        order.verify(sectionRepository).findForUpdateBySectionId(13L);
    }

    @Test
    void createAllowsSixCourses(){
        assertThat(service.create(1L, request(1L, 2L, 3L, 4L, 5L, 6L))).hasSize(6);
    }

    @Test
    void createRequiresAcademicPeriod(){
        assertThatThrownBy(() -> service.create(1L, new EnrollmentRequestDTO(null, List.of(1L, 2L, 3L))))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("academic period");
    }

    @Test
    void createRequiresCourses(){
        assertThatThrownBy(() -> service.create(1L, new EnrollmentRequestDTO(1L, null)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("At least one");
        assertThatThrownBy(() -> service.create(1L, new EnrollmentRequestDTO(1L, List.of())))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createRejectsNullCourseIds(){
        List<Long> ids = new ArrayList<>();
        ids.add(1L);
        ids.add(null);

        assertThatThrownBy(() -> service.create(1L, new EnrollmentRequestDTO(1L, ids)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Every course id");
    }

    @Test
    void createRejectsRepeatedCourses(){
        assertThatThrownBy(() -> service.create(1L, request(1L, 1L, 2L)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("more than once");
    }

    @Test
    void createFailsWhenStudentIsMissing(){
        when(studentRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(9L, request(1L, 2L, 3L))).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createFailsWhenPeriodIsMissing(){
        when(academicPeriodRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(1L, request(1L, 2L, 3L))).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createRejectsClosedPeriod(){
        period.setAcademicPeriodStatus(AcademicPeriodEntity.AcademicPeriodStatus.CLOSED);

        assertThatThrownBy(() -> service.create(1L, request(1L, 2L, 3L)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("closed");
    }

    @Test
    void createRejectsNonRegularStudents(){
        student.setStudentStatus(StudentEntity.StudentStatus.DEFERRED);

        assertThatThrownBy(() -> service.create(1L, request(1L, 2L, 3L)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("regular");
    }

    @Test
    void createRequiresTuition(){
        when(tuitionRepository.existsByStudentAndAcademicPeriod(student, period)).thenReturn(false);

        assertThatThrownBy(() -> service.create(1L, request(1L, 2L, 3L)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("tuition");
        verify(enrollmentRepository, never()).saveAll(anyList());
    }

    @Test
    void createRejectsCoursesOutsideTheStudyplan(){
        assertThatThrownBy(() -> service.create(1L, request(1L, 2L, 99L)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("does not belong");
    }

    @Test
    void createRejectsCoursesAlreadyEnrolledInThePeriod(){
        EnrollmentEntity current = enrollment(50L, courseOf("A1"), sectionOf("A1"), period, null);
        when(enrollmentRepository.findByStudentAndAcademicPeriod(student, period)).thenReturn(List.of(current));

        assertThatThrownBy(() -> service.create(1L, request(1L, 2L, 3L)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("already enrolled");
    }

    @Test
    void createRejectsAlreadyApprovedCourses(){
        givenApprovedInClosedPeriod("A1");

        assertThatThrownBy(() -> service.create(1L, request(1L, 2L, 3L)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("already approved");
    }

    @Test
    void createRejectsCoursesWithoutSectionInThePeriod(){
        sections.remove(sectionOf("A3"));

        assertThatThrownBy(() -> service.create(1L, request(1L, 2L, 3L)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("not offered");
    }

    @Test
    void createRejectsWhenPrerequisiteIsNotApproved(){
        assertThatThrownBy(() -> service.create(1L, request(8L, 2L, 3L)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("prerequisite A1");
    }

    @Test
    void createAcceptsCourseWhenPrerequisiteWasApprovedInAClosedPeriod(){
        givenApprovedInClosedPeriod("A1");
        sections.removeIf(s -> List.of("A4", "A5", "A6", "A7").contains(s.getCourse().getCourseCode()));

        List<EnrollmentResponseDTO> result = service.create(1L, request(8L, 2L, 3L));

        assertThat(result).extracting(EnrollmentResponseDTO::courseCode).contains("B1");
    }

    @Test
    void createRejectsFullSections(){
        when(enrollmentRepository.countBySection(sectionOf("A2"))).thenReturn(30L);

        assertThatThrownBy(() -> service.create(1L, request(1L, 2L, 3L)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("no available capacity");
    }

    @Test
    void createAcceptsTheLastSeat(){
        when(enrollmentRepository.countBySection(sectionOf("A2"))).thenReturn(29L);

        assertThat(service.create(1L, request(1L, 2L, 3L))).hasSize(3);
    }

    @Test
    void createRejectsMoreThanSixCourses(){
        assertThatThrownBy(() -> service.create(1L, request(1L, 2L, 3L, 4L, 5L, 6L, 7L)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("more than 6");
    }

    @Test
    void createRejectsFewerThanThreeCoursesWhenMoreAreAvailable(){
        assertThatThrownBy(() -> service.create(1L, request(1L, 2L)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("at least 3");
    }

    @Test
    void createAllowsFewerThanThreeWhenFewerAreAvailable(){
        sections.removeIf(s -> !List.of("A1", "A2").contains(s.getCourse().getCourseCode()));

        assertThat(service.create(1L, request(1L, 2L))).hasSize(2);
    }

    @Test
    void createCountsTheCurrentEnrollmentsTowardTheMinimum(){
        EnrollmentEntity current = enrollment(50L, courseOf("A1"), sectionOf("A1"), period, null);
        when(enrollmentRepository.findByStudentAndAcademicPeriod(student, period)).thenReturn(List.of(current));

        assertThat(service.create(1L, request(2L, 3L))).hasSize(2);
    }

    @Test
    void createRejectsHigherSemesterWhileLowerSemesterCoursesAreAvailable(){
        givenApprovedInClosedPeriod("A1");

        assertThatThrownBy(() -> service.create(1L, request(8L, 2L, 3L)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("lower-semester");
    }

    @Test
    void createAcceptsHigherSemesterWhenAllLowerOnesAreCovered(){
        givenApprovedInClosedPeriod("A1");
        sections.removeIf(s -> List.of("A5", "A6", "A7").contains(s.getCourse().getCourseCode()));

        assertThat(service.create(1L, request(8L, 2L, 3L, 4L))).hasSize(4);
    }

    @Test
    void createRejectsScheduleConflictBetweenSelectedCourses(){
        sectionOf("A2").setScheduleBlocks(new ArrayList<>(List.of(new SectionEntity.ScheduleBlock(ScheduleDay.M, 1))));
        sectionOf("A1").setScheduleBlocks(new ArrayList<>(List.of(new SectionEntity.ScheduleBlock(ScheduleDay.M, 1))));

        assertThatThrownBy(() -> service.create(1L, request(1L, 2L, 3L)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Schedule conflict in block M1 between A1 and A2.");
    }

    @Test
    void createAsksToKeepTheLowerSemesterCourseWhenSemestersConflict(){
        givenApprovedInClosedPeriod("A1");
        sections.removeIf(s -> List.of("A5", "A6", "A7").contains(s.getCourse().getCourseCode()));
        sectionOf("B1").setScheduleBlocks(new ArrayList<>(List.of(new SectionEntity.ScheduleBlock(ScheduleDay.V, 3))));
        sectionOf("A2").setScheduleBlocks(new ArrayList<>(List.of(new SectionEntity.ScheduleBlock(ScheduleDay.V, 3))));

        assertThatThrownBy(() -> service.create(1L, request(8L, 2L, 3L, 4L)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Schedule conflict in block V3 between B1 (semester 2) and A2 (semester 1). "
                        + "Keep A2, the lower semester course, and remove B1.");
    }

    @Test
    void createAsksToKeepTheLowerSemesterCourseWhenItComesSecond(){
        givenApprovedInClosedPeriod("A1");
        sections.removeIf(s -> List.of("A5", "A6", "A7").contains(s.getCourse().getCourseCode()));
        sectionOf("B1").setScheduleBlocks(new ArrayList<>(List.of(new SectionEntity.ScheduleBlock(ScheduleDay.V, 3))));
        sectionOf("A2").setScheduleBlocks(new ArrayList<>(List.of(new SectionEntity.ScheduleBlock(ScheduleDay.V, 3))));

        assertThatThrownBy(() -> service.create(1L, request(2L, 8L, 3L, 4L)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Schedule conflict in block V3 between A2 (semester 1) and B1 (semester 2). "
                        + "Keep A2, the lower semester course, and remove B1.");
    }

    @Test
    void createRejectsScheduleConflictWithCurrentEnrollments(){
        EnrollmentEntity current = enrollment(50L, courseOf("A1"), sectionOf("A1"), period, null);
        when(enrollmentRepository.findByStudentAndAcademicPeriod(student, period)).thenReturn(List.of(current));
        sectionOf("A2").setScheduleBlocks(new ArrayList<>(sectionOf("A1").getScheduleBlocks()));

        assertThatThrownBy(() -> service.create(1L, request(2L, 3L)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Schedule conflict");
    }

    @Test
    void getAllReturnsEnrollments(){
        when(enrollmentRepository.findAll()).thenReturn(List.of(enrollment(1L, courseOf("A1"), sectionOf("A1"), period, null)));

        assertThat(service.getAll()).hasSize(1);
    }

    @Test
    void getByIdReturnsEnrollmentWithResult(){
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment(1L, courseOf("A1"), sectionOf("A1"), period, 5.0)));

        EnrollmentResponseDTO result = service.getById(1L);

        assertThat(result.result()).isEqualTo(EnrollmentEntity.EnrollmentResult.APPROVED);
    }

    @Test
    void getByIdFailsWhenMissing(){
        when(enrollmentRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(9L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getByStudentReturnsHisEnrollments(){
        when(enrollmentRepository.findByStudent(student)).thenReturn(List.of(enrollment(1L, courseOf("A1"), sectionOf("A1"), period, null)));

        assertThat(service.getByStudent(1L)).hasSize(1);
    }

    @Test
    void getByStudentFailsWhenStudentIsMissing(){
        when(studentRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByStudent(9L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getAcademicHistoryReturnsClosedPeriodEnrollments(){
        givenApprovedInClosedPeriod("A1");

        List<EnrollmentResponseDTO> result = service.getAcademicHistory(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).result()).isEqualTo(EnrollmentEntity.EnrollmentResult.APPROVED);
    }

    @Test
    void getAcademicHistoryFailsWhenStudentIsMissing(){
        when(studentRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAcademicHistory(9L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void availableOfferListsOnlyCoursesWithoutPendingPrerequisites(){
        List<AvailableCourseDTO> result = service.getAvailableOffer(1L, 1L);

        assertThat(result).extracting(AvailableCourseDTO::courseCode)
                .containsExactly("A1", "A2", "A3", "A4", "A5", "A6", "A7");
        assertThat(result.get(0).teacherFullname()).isEqualTo("Teacher One");
        assertThat(result.get(0).availableSeats()).isEqualTo(30);
    }

    @Test
    void availableOfferUnlocksCoursesWhosePrerequisiteIsApproved(){
        givenApprovedInClosedPeriod("A1");

        assertThat(service.getAvailableOffer(1L, 1L)).extracting(AvailableCourseDTO::courseCode)
                .contains("B1").doesNotContain("A1");
    }

    @Test
    void availableOfferHidesCoursesAlreadyEnrolledInThePeriod(){
        EnrollmentEntity current = enrollment(50L, courseOf("A1"), sectionOf("A1"), period, null);
        when(enrollmentRepository.findByStudentAndAcademicPeriod(student, period)).thenReturn(List.of(current));

        assertThat(service.getAvailableOffer(1L, 1L)).extracting(AvailableCourseDTO::courseCode).doesNotContain("A1");
    }

    @Test
    void availableOfferReportsRemainingSeats(){
        when(enrollmentRepository.countBySection(sectionOf("A1"))).thenReturn(28L);

        assertThat(service.getAvailableOffer(1L, 1L).get(0).availableSeats()).isEqualTo(2);
    }

    @Test
    void availableOfferFailsWhenStudentOrPeriodAreMissing(){
        when(studentRepository.findById(9L)).thenReturn(Optional.empty());
        when(academicPeriodRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAvailableOffer(9L, 1L)).isInstanceOf(EntityNotFoundException.class);
        assertThatThrownBy(() -> service.getAvailableOffer(1L, 9L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getByAcademicPeriodReturnsEnrollments(){
        when(enrollmentRepository.findByAcademicPeriod(period)).thenReturn(List.of(enrollment(1L, courseOf("A1"), sectionOf("A1"), period, null)));

        assertThat(service.getByAcademicPeriod(1L)).hasSize(1);
    }

    @Test
    void getByAcademicPeriodFailsWhenPeriodIsMissing(){
        when(academicPeriodRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByAcademicPeriod(9L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getBySectionReturnsEnrollments(){
        SectionEntity section = sectionOf("A1");
        when(sectionRepository.findById(11L)).thenReturn(Optional.of(section));
        when(enrollmentRepository.findBySection(section)).thenReturn(List.of(enrollment(1L, courseOf("A1"), section, period, null)));

        assertThat(service.getBySection(11L)).hasSize(1);
    }

    @Test
    void getBySectionFailsWhenSectionIsMissing(){
        when(sectionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getBySection(99L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getBySectionForTeacherReturnsEnrollmentsToTheTeacherInCharge(){
        SectionEntity section = sectionOf("A1");
        when(sectionRepository.findById(11L)).thenReturn(Optional.of(section));
        when(enrollmentRepository.findBySection(section)).thenReturn(List.of(enrollment(1L, courseOf("A1"), section, period, null)));

        assertThat(service.getBySectionForTeacher(11L, 1L)).hasSize(1);
    }

    @Test
    void getBySectionForTeacherRejectsAnotherTeacher(){
        when(sectionRepository.findById(11L)).thenReturn(Optional.of(sectionOf("A1")));

        assertThatThrownBy(() -> service.getBySectionForTeacher(11L, 2L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void cancelDeletesTheOwnEnrollment(){
        EnrollmentEntity enrollment = enrollment(1L, courseOf("A1"), sectionOf("A1"), period, null);
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));

        service.cancel(1L, 1L);

        verify(enrollmentRepository).delete(enrollment);
    }

    @Test
    void cancelRejectsAnotherStudent(){
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment(1L, courseOf("A1"), sectionOf("A1"), period, null)));

        assertThatThrownBy(() -> service.cancel(1L, 2L)).isInstanceOf(ForbiddenOperationException.class);
        verify(enrollmentRepository, never()).delete(any());
    }

    @Test
    void cancelRejectsClosedPeriod(){
        AcademicPeriodEntity closed = period(9L, AcademicPeriodEntity.AcademicPeriodStatus.CLOSED);
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment(1L, courseOf("A1"), sectionOf("A1"), closed, null)));

        assertThatThrownBy(() -> service.cancel(1L, 1L)).isInstanceOf(IllegalStateException.class).hasMessageContaining("closed");
    }

    @Test
    void cancelRejectsEnrollmentsWithFinalGrade(){
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment(1L, courseOf("A1"), sectionOf("A1"), period, 5.0)));

        assertThatThrownBy(() -> service.cancel(1L, 1L)).isInstanceOf(IllegalStateException.class).hasMessageContaining("final grade");
    }

    @Test
    void cancelFailsWhenEnrollmentIsMissing(){
        when(enrollmentRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancel(9L, 1L)).isInstanceOf(EntityNotFoundException.class);
    }
}
