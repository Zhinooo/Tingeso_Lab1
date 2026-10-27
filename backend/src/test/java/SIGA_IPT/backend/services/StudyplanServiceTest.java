package SIGA_IPT.backend.services;

import SIGA_IPT.backend.dtos.StudyplanDTOs.StudyplanRequestDTO;
import SIGA_IPT.backend.dtos.StudyplanDTOs.StudyplanResponseDTO;
import SIGA_IPT.backend.entities.CareerEntity;
import SIGA_IPT.backend.entities.StudyplanEntity;
import SIGA_IPT.backend.mappers.StudyplanMapper;
import SIGA_IPT.backend.repositories.CareerRepository;
import SIGA_IPT.backend.repositories.CourseRepository;
import SIGA_IPT.backend.repositories.StudentRepository;
import SIGA_IPT.backend.repositories.StudyplanRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

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
class StudyplanServiceTest {

    @Mock
    private StudyplanRepository studyplanRepository;
    @Mock
    private CareerRepository careerRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private CourseRepository courseRepository;

    private StudyplanService service;
    private CareerEntity career;

    @BeforeEach
    void setUp(){
        service = new StudyplanService(studyplanRepository, careerRepository, studentRepository, courseRepository, new StudyplanMapper());
        when(studyplanRepository.save(any(StudyplanEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        career = new CareerEntity();
        career.setCareerId(1L);
        career.setCareerCode("10450");
        when(careerRepository.findByCareerCode("10450")).thenReturn(Optional.of(career));
    }

    private StudyplanEntity plan(Long id, String identifier, StudyplanEntity.StudyplanStatus status){
        StudyplanEntity plan = new StudyplanEntity();
        plan.setStudyplanId(id);
        plan.setStudyplanIdentifier(identifier);
        plan.setCareer(career);
        plan.setStudyplanStatus(status);
        return plan;
    }

    @Test
    void createCurrentPlanDemotesThePreviousOne(){
        StudyplanEntity previous = plan(1L, "2021.3", StudyplanEntity.StudyplanStatus.CURRENT);
        when(studyplanRepository.findByCareerAndStudyplanStatus(career, StudyplanEntity.StudyplanStatus.CURRENT))
                .thenReturn(Optional.of(previous));

        StudyplanResponseDTO result = service.create(new StudyplanRequestDTO("2024.1", "10450", true));

        assertThat(result.studyplanStatus()).isEqualTo(StudyplanEntity.StudyplanStatus.CURRENT);
        assertThat(previous.getStudyplanStatus()).isEqualTo(StudyplanEntity.StudyplanStatus.NOT_CURRENT);
    }

    @Test
    void createCurrentPlanWorksWhenThereIsNoPreviousCurrent(){
        when(studyplanRepository.findByCareerAndStudyplanStatus(career, StudyplanEntity.StudyplanStatus.CURRENT))
                .thenReturn(Optional.empty());

        assertThat(service.create(new StudyplanRequestDTO("2024.1", "10450", true)).studyplanStatus())
                .isEqualTo(StudyplanEntity.StudyplanStatus.CURRENT);
    }

    @Test
    void createNotCurrentPlanKeepsThePreviousCurrent(){
        StudyplanEntity previous = plan(1L, "2021.3", StudyplanEntity.StudyplanStatus.CURRENT);
        when(studyplanRepository.findByCareerAndStudyplanStatus(career, StudyplanEntity.StudyplanStatus.CURRENT))
                .thenReturn(Optional.of(previous));

        StudyplanResponseDTO result = service.create(new StudyplanRequestDTO("2024.1", "10450", false));

        assertThat(result.studyplanStatus()).isEqualTo(StudyplanEntity.StudyplanStatus.NOT_CURRENT);
        assertThat(previous.getStudyplanStatus()).isEqualTo(StudyplanEntity.StudyplanStatus.CURRENT);
    }

    @Test
    void createRejectsBlankIdentifier(){
        assertThatThrownBy(() -> service.create(new StudyplanRequestDTO(" ", "10450", true)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("identifier");
    }

    @Test
    void createRejectsBlankCareerCode(){
        assertThatThrownBy(() -> service.create(new StudyplanRequestDTO("2024.1", "", true)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("career code");
    }

    @Test
    void createRequiresCurrentFlag(){
        assertThatThrownBy(() -> service.create(new StudyplanRequestDTO("2024.1", "10450", null)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("current");
    }

    @Test
    void createFailsWhenCareerDoesNotExist(){
        when(careerRepository.findByCareerCode("X")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new StudyplanRequestDTO("2024.1", "X", true)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createRejectsDuplicatedIdentifierInTheCareer(){
        when(studyplanRepository.existsByStudyplanIdentifierAndCareer("2024.1", career)).thenReturn(true);

        assertThatThrownBy(() -> service.create(new StudyplanRequestDTO("2024.1", "10450", true)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("already has");
        verify(studyplanRepository, never()).save(any());
    }

    @Test
    void getAllReturnsPlans(){
        when(studyplanRepository.findAll()).thenReturn(List.of(plan(1L, "A", StudyplanEntity.StudyplanStatus.CURRENT)));

        assertThat(service.getAll()).hasSize(1);
    }

    @Test
    void getByIdReturnsPlan(){
        when(studyplanRepository.findById(1L)).thenReturn(Optional.of(plan(1L, "A", StudyplanEntity.StudyplanStatus.CURRENT)));

        assertThat(service.getById(1L).careerCode()).isEqualTo("10450");
    }

    @Test
    void getByIdFailsWhenMissing(){
        when(studyplanRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(9L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateChangesIdentifier(){
        when(studyplanRepository.findById(1L)).thenReturn(Optional.of(plan(1L, "A", StudyplanEntity.StudyplanStatus.CURRENT)));

        assertThat(service.update(1L, new StudyplanRequestDTO("B", "10450", null)).studyplanIdentifier()).isEqualTo("B");
    }

    @Test
    void updateAcceptsNullCareerCode(){
        when(studyplanRepository.findById(1L)).thenReturn(Optional.of(plan(1L, "A", StudyplanEntity.StudyplanStatus.CURRENT)));

        assertThat(service.update(1L, new StudyplanRequestDTO("A", null, null)).studyplanIdentifier()).isEqualTo("A");
    }

    @Test
    void updateRejectsChangingTheCareer(){
        when(studyplanRepository.findById(1L)).thenReturn(Optional.of(plan(1L, "A", StudyplanEntity.StudyplanStatus.CURRENT)));

        assertThatThrownBy(() -> service.update(1L, new StudyplanRequestDTO("A", "OTHER", null)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("career");
    }

    @Test
    void updateRejectsIdentifierAlreadyUsedInTheCareer(){
        when(studyplanRepository.findById(1L)).thenReturn(Optional.of(plan(1L, "A", StudyplanEntity.StudyplanStatus.CURRENT)));
        when(studyplanRepository.existsByStudyplanIdentifierAndCareer("B", career)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, new StudyplanRequestDTO("B", "10450", null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateRejectsBlankIdentifier(){
        assertThatThrownBy(() -> service.update(1L, new StudyplanRequestDTO("", "10450", null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void changeStatusToCurrentDemotesTheOtherCurrentPlan(){
        StudyplanEntity target = plan(2L, "B", StudyplanEntity.StudyplanStatus.NOT_CURRENT);
        StudyplanEntity other = plan(1L, "A", StudyplanEntity.StudyplanStatus.CURRENT);
        when(studyplanRepository.findById(2L)).thenReturn(Optional.of(target));
        when(studyplanRepository.findByCareerAndStudyplanStatus(career, StudyplanEntity.StudyplanStatus.CURRENT))
                .thenReturn(Optional.of(other));

        StudyplanResponseDTO result = service.changeStatus(2L, StudyplanEntity.StudyplanStatus.CURRENT);

        assertThat(result.studyplanStatus()).isEqualTo(StudyplanEntity.StudyplanStatus.CURRENT);
        assertThat(other.getStudyplanStatus()).isEqualTo(StudyplanEntity.StudyplanStatus.NOT_CURRENT);
    }

    @Test
    void changeStatusToCurrentDoesNotDemoteItself(){
        StudyplanEntity target = plan(1L, "A", StudyplanEntity.StudyplanStatus.CURRENT);
        when(studyplanRepository.findById(1L)).thenReturn(Optional.of(target));
        when(studyplanRepository.findByCareerAndStudyplanStatus(career, StudyplanEntity.StudyplanStatus.CURRENT))
                .thenReturn(Optional.of(target));

        assertThat(service.changeStatus(1L, StudyplanEntity.StudyplanStatus.CURRENT).studyplanStatus())
                .isEqualTo(StudyplanEntity.StudyplanStatus.CURRENT);
    }

    @Test
    void changeStatusToNotCurrentJustUpdatesThePlan(){
        when(studyplanRepository.findById(1L)).thenReturn(Optional.of(plan(1L, "A", StudyplanEntity.StudyplanStatus.CURRENT)));

        assertThat(service.changeStatus(1L, StudyplanEntity.StudyplanStatus.NOT_CURRENT).studyplanStatus())
                .isEqualTo(StudyplanEntity.StudyplanStatus.NOT_CURRENT);
    }

    @Test
    void changeStatusRejectsNull(){
        assertThatThrownBy(() -> service.changeStatus(1L, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deleteRemovesFreePlan(){
        StudyplanEntity plan = plan(1L, "A", StudyplanEntity.StudyplanStatus.NOT_CURRENT);
        when(studyplanRepository.findById(1L)).thenReturn(Optional.of(plan));

        service.delete(1L);

        verify(studyplanRepository).delete(plan);
    }

    @Test
    void deleteRejectsPlanWithStudents(){
        StudyplanEntity plan = plan(1L, "A", StudyplanEntity.StudyplanStatus.NOT_CURRENT);
        when(studyplanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(studentRepository.existsByStudyplan(plan)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(IllegalStateException.class).hasMessageContaining("students");
    }

    @Test
    void deleteRejectsPlanWithCourses(){
        StudyplanEntity plan = plan(1L, "A", StudyplanEntity.StudyplanStatus.NOT_CURRENT);
        when(studyplanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(courseRepository.existsByStudyplan(plan)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(IllegalStateException.class).hasMessageContaining("courses");
        verify(studyplanRepository, never()).delete(any());
    }
}
