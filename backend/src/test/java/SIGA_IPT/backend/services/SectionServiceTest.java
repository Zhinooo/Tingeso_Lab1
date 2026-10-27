package SIGA_IPT.backend.services;

import SIGA_IPT.backend.dtos.SectionDTOs.ScheduleBlockDTO;
import SIGA_IPT.backend.dtos.SectionDTOs.SectionRequestDTO;
import SIGA_IPT.backend.dtos.SectionDTOs.SectionResponseDTO;
import SIGA_IPT.backend.dtos.SectionDTOs.SectionUpdateRequestDTO;
import SIGA_IPT.backend.entities.AcademicPeriodEntity;
import SIGA_IPT.backend.entities.CourseEntity;
import SIGA_IPT.backend.entities.SectionEntity;
import SIGA_IPT.backend.entities.SectionEntity.ScheduleDay;
import SIGA_IPT.backend.entities.TeacherEntity;
import SIGA_IPT.backend.mappers.SectionMapper;
import SIGA_IPT.backend.repositories.AcademicPeriodRepository;
import SIGA_IPT.backend.repositories.CourseRepository;
import SIGA_IPT.backend.repositories.EnrollmentRepository;
import SIGA_IPT.backend.repositories.SectionRepository;
import SIGA_IPT.backend.repositories.TeacherRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SectionServiceTest {

    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private AcademicPeriodRepository academicPeriodRepository;
    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;

    private SectionService service;
    private CourseEntity course;
    private AcademicPeriodEntity period;
    private TeacherEntity teacher;

    @BeforeEach
    void setUp(){
        service = new SectionService(sectionRepository, courseRepository, academicPeriodRepository, teacherRepository,
                enrollmentRepository, new SectionMapper());
        when(sectionRepository.save(any(SectionEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        course = new CourseEntity();
        course.setCourseId(1L);
        course.setCourseCode("ZZ101");
        course.setCourseName("Course");
        course.setTheoryHours(2);
        course.setExerciseHours(2);
        course.setLabHours(0);
        period = new AcademicPeriodEntity();
        period.setAcademicPeriodId(1L);
        period.setAcademicYear(2030);
        period.setAcademicSemester(1);
        period.setAcademicPeriodStatus(AcademicPeriodEntity.AcademicPeriodStatus.OPEN);
        teacher = teacher(1L, TeacherEntity.TeacherStatus.ACTIVE);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(academicPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
    }

    private TeacherEntity teacher(Long id, TeacherEntity.TeacherStatus status){
        TeacherEntity teacher = new TeacherEntity();
        teacher.setTeacherId(id);
        teacher.setTeacherFullname("Teacher " + id);
        teacher.setTeacherStatus(status);
        return teacher;
    }

    private ScheduleBlockDTO block(ScheduleDay day, Integer module){
        return new ScheduleBlockDTO(day, module);
    }

    private List<ScheduleBlockDTO> twoBlocks(){
        return List.of(block(ScheduleDay.L, 1), block(ScheduleDay.W, 1));
    }

    private SectionRequestDTO request(Long courseId, Long periodId, Long teacherId, Integer capacity, List<ScheduleBlockDTO> blocks){
        return new SectionRequestDTO(courseId, periodId, teacherId, capacity, blocks);
    }

    private SectionRequestDTO validRequest(){
        return request(1L, 1L, 1L, 30, twoBlocks());
    }

    private SectionEntity section(Long id, TeacherEntity teacher, ScheduleDay day1, int module1, ScheduleDay day2, int module2){
        SectionEntity section = new SectionEntity();
        section.setSectionId(id);
        section.setCourse(course);
        section.setAcademicPeriod(period);
        section.setTeacher(teacher);
        section.setSectionCapacity(30);
        section.setScheduleBlocks(new ArrayList<>(List.of(
                new SectionEntity.ScheduleBlock(day1, module1),
                new SectionEntity.ScheduleBlock(day2, module2))));
        return section;
    }

    private SectionEntity existingSection(){
        return section(5L, teacher, ScheduleDay.L, 1, ScheduleDay.W, 1);
    }

    @Test
    void createSavesSectionWithItsBlocks(){
        SectionResponseDTO result = service.create(validRequest());

        assertThat(result.scheduleBlocks()).containsExactly("L1", "W1");
        assertThat(result.sectionCapacity()).isEqualTo(30);
        assertThat(result.teacherId()).isEqualTo(1L);
    }

    @Test
    void createRequiresCourse(){
        assertThatThrownBy(() -> service.create(request(null, 1L, 1L, 30, twoBlocks())))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("course");
    }

    @Test
    void createRequiresAcademicPeriod(){
        assertThatThrownBy(() -> service.create(request(1L, null, 1L, 30, twoBlocks())))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("academic period");
    }

    @Test
    void createRequiresTeacher(){
        assertThatThrownBy(() -> service.create(request(1L, 1L, null, 30, twoBlocks())))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("teacher");
    }

    @Test
    void createRequiresCapacity(){
        assertThatThrownBy(() -> service.create(request(1L, 1L, 1L, null, twoBlocks())))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("capacity is required");
    }

    @Test
    void createRejectsCapacityBelowOne(){
        assertThatThrownBy(() -> service.create(request(1L, 1L, 1L, 0, twoBlocks())))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("between 1 and 60");
    }

    @Test
    void createRejectsCapacityAboveSixty(){
        assertThatThrownBy(() -> service.create(request(1L, 1L, 1L, 61, twoBlocks())))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("between 1 and 60");
    }

    @Test
    void createAcceptsBoundaryCapacities(){
        assertThat(service.create(request(1L, 1L, 1L, 1, twoBlocks())).sectionCapacity()).isEqualTo(1);
        when(sectionRepository.existsByCourseAndAcademicPeriod(course, period)).thenReturn(false);
        assertThat(service.create(request(1L, 1L, 1L, 60, twoBlocks())).sectionCapacity()).isEqualTo(60);
    }

    @Test
    void createRejectsMissingBlocks(){
        assertThatThrownBy(() -> service.create(request(1L, 1L, 1L, 30, null)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("at least one");
        assertThatThrownBy(() -> service.create(request(1L, 1L, 1L, 30, List.of())))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createRejectsBlockWithoutDay(){
        assertThatThrownBy(() -> service.create(request(1L, 1L, 1L, 30, List.of(block(null, 1), block(ScheduleDay.W, 1)))))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("valid day");
    }

    @Test
    void createRejectsNullBlock(){
        List<ScheduleBlockDTO> blocks = new ArrayList<>();
        blocks.add(null);
        blocks.add(block(ScheduleDay.W, 1));

        assertThatThrownBy(() -> service.create(request(1L, 1L, 1L, 30, blocks)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("valid day");
    }

    @Test
    void createRejectsModuleOutOfRange(){
        assertThatThrownBy(() -> service.create(request(1L, 1L, 1L, 30, List.of(block(ScheduleDay.L, 0), block(ScheduleDay.W, 1)))))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("module between 1 and 6");
        assertThatThrownBy(() -> service.create(request(1L, 1L, 1L, 30, List.of(block(ScheduleDay.L, 7), block(ScheduleDay.W, 1)))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.create(request(1L, 1L, 1L, 30, List.of(block(ScheduleDay.L, null), block(ScheduleDay.W, 1)))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createRejectsDuplicatedBlocks(){
        assertThatThrownBy(() -> service.create(request(1L, 1L, 1L, 30, List.of(block(ScheduleDay.L, 1), block(ScheduleDay.L, 1)))))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Duplicated");
    }

    @Test
    void createFailsWhenCourseIsMissing(){
        when(courseRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request(9L, 1L, 1L, 30, twoBlocks()))).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createFailsWhenPeriodIsMissing(){
        when(academicPeriodRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request(1L, 9L, 1L, 30, twoBlocks()))).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createFailsWhenTeacherIsMissing(){
        when(teacherRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request(1L, 1L, 9L, 30, twoBlocks()))).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createRejectsClosedPeriod(){
        period.setAcademicPeriodStatus(AcademicPeriodEntity.AcademicPeriodStatus.CLOSED);

        assertThatThrownBy(() -> service.create(validRequest()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("closed");
    }

    @Test
    void createRejectsInactiveTeacher(){
        teacher.setTeacherStatus(TeacherEntity.TeacherStatus.INACTIVE);

        assertThatThrownBy(() -> service.create(validRequest()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("active");
    }

    @Test
    void createRejectsSecondSectionOfTheSameCourseInThePeriod(){
        when(sectionRepository.existsByCourseAndAcademicPeriod(course, period)).thenReturn(true);

        assertThatThrownBy(() -> service.create(validRequest()))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("already has a section");
        verify(sectionRepository, never()).save(any());
    }

    @Test
    void createRejectsBlockCountThatDoesNotMatchTheWeeklyHours(){
        assertThatThrownBy(() -> service.create(request(1L, 1L, 1L, 30, List.of(block(ScheduleDay.L, 1)))))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("must match");
    }

    @Test
    void createRejectsTeacherScheduleConflict(){
        SectionEntity other = section(7L, teacher, ScheduleDay.L, 1, ScheduleDay.J, 3);
        when(sectionRepository.findByTeacherAndAcademicPeriod(teacher, period)).thenReturn(List.of(other));

        assertThatThrownBy(() -> service.create(validRequest()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Schedule conflict");
    }

    @Test
    void createAllowsTeacherWithOtherBlocks(){
        SectionEntity other = section(7L, teacher, ScheduleDay.M, 2, ScheduleDay.J, 3);
        when(sectionRepository.findByTeacherAndAcademicPeriod(teacher, period)).thenReturn(List.of(other));

        assertThat(service.create(validRequest()).scheduleBlocks()).hasSize(2);
    }

    @Test
    void getAllReturnsSections(){
        when(sectionRepository.findAll()).thenReturn(List.of(existingSection()));

        assertThat(service.getAll()).hasSize(1);
    }

    @Test
    void getByIdReturnsSection(){
        when(sectionRepository.findById(5L)).thenReturn(Optional.of(existingSection()));

        assertThat(service.getById(5L).sectionId()).isEqualTo(5L);
    }

    @Test
    void getByIdFailsWhenMissing(){
        when(sectionRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(9L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getByAcademicPeriodReturnsItsSections(){
        when(sectionRepository.findByAcademicPeriod(period)).thenReturn(List.of(existingSection()));

        assertThat(service.getByAcademicPeriod(1L)).hasSize(1);
    }

    @Test
    void getByAcademicPeriodFailsWhenPeriodIsMissing(){
        when(academicPeriodRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByAcademicPeriod(9L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getByTeacherReturnsHisSections(){
        when(sectionRepository.findByTeacher(teacher)).thenReturn(List.of(existingSection()));

        assertThat(service.getByTeacher(1L)).hasSize(1);
    }

    @Test
    void getByTeacherFailsWhenTeacherIsMissing(){
        when(teacherRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByTeacher(9L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateChangesCapacityAndSchedule(){
        when(sectionRepository.findById(5L)).thenReturn(Optional.of(existingSection()));

        SectionResponseDTO result = service.update(5L,
                new SectionUpdateRequestDTO(1L, 40, List.of(block(ScheduleDay.M, 2), block(ScheduleDay.J, 2))));

        assertThat(result.sectionCapacity()).isEqualTo(40);
        assertThat(result.scheduleBlocks()).containsExactly("M2", "J2");
    }

    @Test
    void updateRequiresTeacher(){
        assertThatThrownBy(() -> service.update(5L, new SectionUpdateRequestDTO(null, 30, twoBlocks())))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("teacher");
    }

    @Test
    void updateFailsWhenSectionIsMissing(){
        when(sectionRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(9L, new SectionUpdateRequestDTO(1L, 30, twoBlocks())))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateRejectsClosedPeriod(){
        when(sectionRepository.findById(5L)).thenReturn(Optional.of(existingSection()));
        period.setAcademicPeriodStatus(AcademicPeriodEntity.AcademicPeriodStatus.CLOSED);

        assertThatThrownBy(() -> service.update(5L, new SectionUpdateRequestDTO(1L, 30, twoBlocks())))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("closed");
    }

    @Test
    void updateAssignsAnotherActiveTeacher(){
        TeacherEntity other = teacher(2L, TeacherEntity.TeacherStatus.ACTIVE);
        when(teacherRepository.findById(2L)).thenReturn(Optional.of(other));
        when(sectionRepository.findById(5L)).thenReturn(Optional.of(existingSection()));

        assertThat(service.update(5L, new SectionUpdateRequestDTO(2L, 30, twoBlocks())).teacherId()).isEqualTo(2L);
    }

    @Test
    void updateRejectsAssigningAnInactiveTeacher(){
        TeacherEntity other = teacher(2L, TeacherEntity.TeacherStatus.INACTIVE);
        when(teacherRepository.findById(2L)).thenReturn(Optional.of(other));
        when(sectionRepository.findById(5L)).thenReturn(Optional.of(existingSection()));

        assertThatThrownBy(() -> service.update(5L, new SectionUpdateRequestDTO(2L, 30, twoBlocks())))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("active");
    }

    @Test
    void updateRejectsBlockCountMismatch(){
        when(sectionRepository.findById(5L)).thenReturn(Optional.of(existingSection()));

        assertThatThrownBy(() -> service.update(5L, new SectionUpdateRequestDTO(1L, 30, List.of(block(ScheduleDay.L, 1)))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateRejectsChangingTheScheduleWhenStudentsAreEnrolled(){
        SectionEntity existing = existingSection();
        when(sectionRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(enrollmentRepository.countBySection(existing)).thenReturn(3L);

        assertThatThrownBy(() -> service.update(5L,
                new SectionUpdateRequestDTO(1L, 30, List.of(block(ScheduleDay.M, 2), block(ScheduleDay.J, 2)))))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("enrolled");
    }

    @Test
    void updateRejectsChangingTheTeacherWhenStudentsAreEnrolled(){
        TeacherEntity other = teacher(2L, TeacherEntity.TeacherStatus.ACTIVE);
        when(teacherRepository.findById(2L)).thenReturn(Optional.of(other));
        SectionEntity existing = existingSection();
        when(sectionRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(enrollmentRepository.countBySection(existing)).thenReturn(3L);

        assertThatThrownBy(() -> service.update(5L, new SectionUpdateRequestDTO(2L, 30, twoBlocks())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void updateAllowsChangingOnlyTheCapacityWhenStudentsAreEnrolled(){
        SectionEntity existing = existingSection();
        when(sectionRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(enrollmentRepository.countBySection(existing)).thenReturn(3L);

        assertThat(service.update(5L, new SectionUpdateRequestDTO(1L, 10, twoBlocks())).sectionCapacity()).isEqualTo(10);
    }

    @Test
    void updateRejectsCapacityBelowTheEnrolledStudents(){
        SectionEntity existing = existingSection();
        when(sectionRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(enrollmentRepository.countBySection(existing)).thenReturn(5L);

        assertThatThrownBy(() -> service.update(5L, new SectionUpdateRequestDTO(1L, 4, twoBlocks())))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("capacity");
    }

    @Test
    void updateRejectsTeacherScheduleConflictWithAnotherSection(){
        SectionEntity existing = existingSection();
        SectionEntity other = section(7L, teacher, ScheduleDay.M, 2, ScheduleDay.J, 2);
        when(sectionRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(sectionRepository.findByTeacherAndAcademicPeriod(teacher, period)).thenReturn(List.of(existing, other));

        assertThatThrownBy(() -> service.update(5L,
                new SectionUpdateRequestDTO(1L, 30, List.of(block(ScheduleDay.M, 2), block(ScheduleDay.V, 3)))))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Schedule conflict");
    }

    @Test
    void updateIgnoresItsOwnBlocksWhenCheckingConflicts(){
        SectionEntity existing = existingSection();
        when(sectionRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(sectionRepository.findByTeacherAndAcademicPeriod(teacher, period)).thenReturn(List.of(existing));

        assertThat(service.update(5L, new SectionUpdateRequestDTO(1L, 35, twoBlocks())).sectionCapacity()).isEqualTo(35);
    }

    @Test
    void deleteRemovesSectionWithoutEnrollments(){
        SectionEntity existing = existingSection();
        when(sectionRepository.findById(5L)).thenReturn(Optional.of(existing));

        service.delete(5L);

        verify(sectionRepository).delete(existing);
    }

    @Test
    void deleteRejectsClosedPeriod(){
        when(sectionRepository.findById(5L)).thenReturn(Optional.of(existingSection()));
        period.setAcademicPeriodStatus(AcademicPeriodEntity.AcademicPeriodStatus.CLOSED);

        assertThatThrownBy(() -> service.delete(5L)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deleteRejectsSectionWithEnrollments(){
        SectionEntity existing = existingSection();
        when(sectionRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(enrollmentRepository.existsBySection(existing)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(5L)).isInstanceOf(IllegalStateException.class).hasMessageContaining("enrolled");
        verify(sectionRepository, never()).delete(any());
    }
}
