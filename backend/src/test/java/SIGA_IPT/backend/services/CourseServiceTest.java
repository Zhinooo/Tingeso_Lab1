package SIGA_IPT.backend.services;

import SIGA_IPT.backend.dtos.CourseDTOs.CourseRequestDTO;
import SIGA_IPT.backend.dtos.CourseDTOs.CourseResponseDTO;
import SIGA_IPT.backend.entities.CourseEntity;
import SIGA_IPT.backend.entities.StudyplanEntity;
import SIGA_IPT.backend.mappers.CourseMapper;
import SIGA_IPT.backend.repositories.CourseRepository;
import SIGA_IPT.backend.repositories.EnrollmentRepository;
import SIGA_IPT.backend.repositories.SectionRepository;
import SIGA_IPT.backend.repositories.StudyplanRepository;
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
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private StudyplanRepository studyplanRepository;
    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;

    private CourseService service;
    private StudyplanEntity plan;

    @BeforeEach
    void setUp(){
        service = new CourseService(courseRepository, studyplanRepository, sectionRepository, enrollmentRepository, new CourseMapper());
        when(courseRepository.save(any(CourseEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        plan = new StudyplanEntity();
        plan.setStudyplanId(1L);
        plan.setStudyplanIdentifier("2021.3");
        when(studyplanRepository.findById(1L)).thenReturn(Optional.of(plan));
    }

    private CourseEntity course(Long id, String code, int semester){
        CourseEntity course = new CourseEntity();
        course.setCourseId(id);
        course.setCourseCode(code);
        course.setCourseName("Name " + code);
        course.setSemester(semester);
        course.setTheoryHours(2);
        course.setExerciseHours(2);
        course.setLabHours(0);
        course.setSct(5);
        course.setStudyplan(plan);
        course.setPrerequisites(new ArrayList<>());
        return course;
    }

    private CourseRequestDTO dto(String code, Integer semester, Integer theory, Integer exercise, Integer lab, Integer sct, Long planId, List<String> prerequisites){
        return new CourseRequestDTO(code, "Name " + code, semester, theory, exercise, lab, sct, planId, prerequisites);
    }

    private CourseRequestDTO validDto(){
        return dto("ZZ101", 1, 2, 2, 0, 5, 1L, List.of());
    }

    @Test
    void createSavesCourseWithoutPrerequisites(){
        CourseResponseDTO result = service.create(validDto());

        assertThat(result.courseCode()).isEqualTo("ZZ101");
        assertThat(result.studyplanIdentifier()).isEqualTo("2021.3");
        assertThat(result.prerequisiteCodes()).isEmpty();
    }

    @Test
    void createAcceptsNullPrerequisiteList(){
        assertThat(service.create(dto("ZZ101", 1, 2, 2, 0, 5, 1L, null)).prerequisiteCodes()).isEmpty();
    }

    @Test
    void createResolvesPrerequisitesFromEarlierSemesters(){
        when(courseRepository.findByCourseCodeAndStudyplan("ZZ101", plan)).thenReturn(Optional.of(course(10L, "ZZ101", 1)));

        CourseResponseDTO result = service.create(dto("ZZ201", 2, 2, 2, 0, 5, 1L, List.of("ZZ101")));

        assertThat(result.prerequisiteCodes()).containsExactly("ZZ101");
    }

    @Test
    void createRejectsBlankCode(){
        assertThatThrownBy(() -> service.create(dto(" ", 1, 2, 2, 0, 5, 1L, null)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("code");
    }

    @Test
    void createRejectsBlankName(){
        CourseRequestDTO noName = new CourseRequestDTO("Z", null, 1, 2, 2, 0, 5, 1L, null);

        assertThatThrownBy(() -> service.create(noName)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("name");
    }

    @Test
    void createRequiresSemester(){
        assertThatThrownBy(() -> service.create(dto("Z", null, 2, 2, 0, 5, 1L, null)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("semester is required");
    }

    @Test
    void createRejectsSemesterBelowRange(){
        assertThatThrownBy(() -> service.create(dto("Z", 0, 2, 2, 0, 5, 1L, null)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("between 1 and 4");
    }

    @Test
    void createRejectsSemesterAboveRange(){
        assertThatThrownBy(() -> service.create(dto("Z", 5, 2, 2, 0, 5, 1L, null)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("between 1 and 4");
    }

    @Test
    void createRequiresAllHourFields(){
        assertThatThrownBy(() -> service.create(dto("Z", 1, null, 2, 0, 5, 1L, null)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("required");
        assertThatThrownBy(() -> service.create(dto("Z", 1, 2, null, 0, 5, 1L, null)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.create(dto("Z", 1, 2, 2, null, 5, 1L, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createRejectsNegativeHours(){
        assertThatThrownBy(() -> service.create(dto("Z", 1, -1, 2, 0, 5, 1L, null)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("negative");
    }

    @Test
    void createRejectsWeeklyHoursOfEightOrMore(){
        assertThatThrownBy(() -> service.create(dto("Z", 1, 4, 2, 2, 5, 1L, null)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("less than 8");
    }

    @Test
    void createAcceptsWeeklyHoursOfSeven(){
        assertThat(service.create(dto("Z", 1, 4, 2, 1, 5, 1L, null)).theoryHours()).isEqualTo(4);
    }

    @Test
    void createRequiresSct(){
        assertThatThrownBy(() -> service.create(dto("Z", 1, 2, 2, 0, null, 1L, null)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("SCT is required");
    }

    @Test
    void createRejectsSctOutOfRange(){
        assertThatThrownBy(() -> service.create(dto("Z", 1, 2, 2, 0, 0, 1L, null)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("SCT");
        assertThatThrownBy(() -> service.create(dto("Z", 1, 2, 2, 0, 8, 1L, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createRequiresStudyplan(){
        assertThatThrownBy(() -> service.create(dto("Z", 1, 2, 2, 0, 5, null, null)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("study plan");
    }

    @Test
    void createFailsWhenStudyplanDoesNotExist(){
        when(studyplanRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(dto("Z", 1, 2, 2, 0, 5, 9L, null)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createRejectsDuplicatedCodeInThePlan(){
        when(courseRepository.existsByCourseCodeAndStudyplan("ZZ101", plan)).thenReturn(true);

        assertThatThrownBy(() -> service.create(validDto()))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("already has a course");
        verify(courseRepository, never()).save(any());
    }

    @Test
    void createRejectsMoreThanThreePrerequisites(){
        assertThatThrownBy(() -> service.create(dto("Z", 4, 2, 2, 0, 5, 1L, List.of("A", "B", "C", "D"))))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("at most 3");
    }

    @Test
    void createRejectsRepeatedPrerequisites(){
        assertThatThrownBy(() -> service.create(dto("Z", 4, 2, 2, 0, 5, 1L, List.of("A", "A"))))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("duplicates");
    }

    @Test
    void createRejectsUnknownPrerequisite(){
        when(courseRepository.findByCourseCodeAndStudyplan("NOPE", plan)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(dto("Z", 2, 2, 2, 0, 5, 1L, List.of("NOPE"))))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createRejectsPrerequisiteFromTheSameOrLaterSemester(){
        when(courseRepository.findByCourseCodeAndStudyplan("ZZ201", plan)).thenReturn(Optional.of(course(10L, "ZZ201", 2)));

        assertThatThrownBy(() -> service.create(dto("ZZ202", 2, 2, 2, 0, 5, 1L, List.of("ZZ201"))))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("earlier semester");
    }

    @Test
    void getAllReturnsCourses(){
        when(courseRepository.findAll()).thenReturn(List.of(course(1L, "A", 1)));

        assertThat(service.getAll()).hasSize(1);
    }

    @Test
    void getByIdReturnsCourse(){
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course(1L, "A", 1)));

        assertThat(service.getById(1L).courseCode()).isEqualTo("A");
    }

    @Test
    void getByIdFailsWhenMissing(){
        when(courseRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(9L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getByStudyplanReturnsItsCourses(){
        when(courseRepository.findByStudyplan(plan)).thenReturn(List.of(course(1L, "A", 1), course(2L, "B", 2)));

        assertThat(service.getByStudyplan(1L)).hasSize(2);
    }

    @Test
    void getByStudyplanFailsWhenPlanIsMissing(){
        when(studyplanRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByStudyplan(9L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateChangesNameAndSct(){
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course(1L, "ZZ101", 1)));

        CourseResponseDTO result = service.update(1L, dto("ZZ101", 1, 2, 2, 0, 6, null, null));

        assertThat(result.sct()).isEqualTo(6);
    }

    @Test
    void updateAcceptsTheSameStudyplanId(){
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course(1L, "ZZ101", 1)));

        assertThat(service.update(1L, dto("ZZ101", 1, 2, 2, 0, 5, 1L, null)).courseId()).isEqualTo(1L);
    }

    @Test
    void updateRejectsChangingTheStudyplan(){
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course(1L, "ZZ101", 1)));

        assertThatThrownBy(() -> service.update(1L, dto("ZZ101", 1, 2, 2, 0, 5, 2L, null)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("study plan");
    }

    @Test
    void updateRejectsCoursesWithAcademicHistory(){
        CourseEntity existing = course(1L, "ZZ101", 1);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(enrollmentRepository.existsByCourse(existing)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, validDto()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("academic history");
    }

    @Test
    void updateRejectsCodeAlreadyUsedInThePlan(){
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course(1L, "ZZ101", 1)));
        when(courseRepository.existsByCourseCodeAndStudyplan("ZZ999", plan)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, dto("ZZ999", 1, 2, 2, 0, 5, null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateAllowsAFreeNewCode(){
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course(1L, "ZZ101", 1)));

        assertThat(service.update(1L, dto("ZZ777", 1, 2, 2, 0, 5, null, null)).courseCode()).isEqualTo("ZZ777");
    }

    @Test
    void updateRejectsChangingSemesterWhenCourseIsAPrerequisite(){
        CourseEntity existing = course(1L, "ZZ101", 1);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(courseRepository.isPrerequisiteOfAnyCourse(existing)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, dto("ZZ101", 2, 2, 2, 0, 5, null, null)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("prerequisite");
    }

    @Test
    void updateChangesSemesterWhenCourseIsNotAPrerequisite(){
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course(1L, "ZZ101", 1)));

        assertThat(service.update(1L, dto("ZZ101", 2, 2, 2, 0, 5, null, null)).semester()).isEqualTo(2);
    }

    @Test
    void updateRejectsChangingHoursWhenCourseHasSections(){
        CourseEntity existing = course(1L, "ZZ101", 1);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(sectionRepository.existsByCourse(existing)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, dto("ZZ101", 1, 2, 1, 0, 5, null, null)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("weekly hours");
    }

    @Test
    void updateKeepsHoursEvenWhenCourseHasSections(){
        CourseEntity existing = course(1L, "ZZ101", 1);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(sectionRepository.existsByCourse(existing)).thenReturn(true);

        assertThat(service.update(1L, dto("ZZ101", 1, 2, 2, 0, 6, null, null)).sct()).isEqualTo(6);
    }

    @Test
    void updateReplacesPrerequisites(){
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course(2L, "ZZ201", 2)));
        when(courseRepository.findByCourseCodeAndStudyplan("ZZ101", plan)).thenReturn(Optional.of(course(1L, "ZZ101", 1)));

        CourseResponseDTO result = service.update(2L, dto("ZZ201", 2, 2, 2, 0, 5, null, List.of("ZZ101")));

        assertThat(result.prerequisiteCodes()).containsExactly("ZZ101");
    }

    @Test
    void deleteRemovesFreeCourse(){
        CourseEntity existing = course(1L, "ZZ101", 1);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(existing));

        service.delete(1L);

        verify(courseRepository).delete(existing);
    }

    @Test
    void deleteRejectsCourseWithHistory(){
        CourseEntity existing = course(1L, "ZZ101", 1);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(enrollmentRepository.existsByCourse(existing)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(IllegalStateException.class).hasMessageContaining("history");
    }

    @Test
    void deleteRejectsCourseWithSections(){
        CourseEntity existing = course(1L, "ZZ101", 1);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(sectionRepository.existsByCourse(existing)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(IllegalStateException.class).hasMessageContaining("sections");
    }

    @Test
    void deleteRejectsCourseThatIsAPrerequisite(){
        CourseEntity existing = course(1L, "ZZ101", 1);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(courseRepository.isPrerequisiteOfAnyCourse(existing)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(IllegalStateException.class).hasMessageContaining("prerequisite");
        verify(courseRepository, never()).delete(any());
    }
}
