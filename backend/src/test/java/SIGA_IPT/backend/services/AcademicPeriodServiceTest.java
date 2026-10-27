package SIGA_IPT.backend.services;

import SIGA_IPT.backend.dtos.AcademicPeriodDTOs.AcademicPeriodRequestDTO;
import SIGA_IPT.backend.dtos.AcademicPeriodDTOs.AcademicPeriodResponseDTO;
import SIGA_IPT.backend.entities.AcademicPeriodEntity;
import SIGA_IPT.backend.mappers.AcademicPeriodMapper;
import SIGA_IPT.backend.repositories.AcademicPeriodRepository;
import SIGA_IPT.backend.repositories.EnrollmentRepository;
import SIGA_IPT.backend.repositories.SectionRepository;
import SIGA_IPT.backend.repositories.TuitionRepository;
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
class AcademicPeriodServiceTest {

    @Mock
    private AcademicPeriodRepository academicPeriodRepository;
    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private TuitionRepository tuitionRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;

    private AcademicPeriodService service;

    @BeforeEach
    void setUp(){
        service = new AcademicPeriodService(academicPeriodRepository, sectionRepository, tuitionRepository,
                enrollmentRepository, new AcademicPeriodMapper());
        when(academicPeriodRepository.save(any(AcademicPeriodEntity.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private AcademicPeriodEntity period(Long id, int year, int semester, AcademicPeriodEntity.AcademicPeriodStatus status){
        AcademicPeriodEntity period = new AcademicPeriodEntity();
        period.setAcademicPeriodId(id);
        period.setAcademicYear(year);
        period.setAcademicSemester(semester);
        period.setAcademicPeriodStatus(status);
        return period;
    }

    private AcademicPeriodEntity openPeriod(){
        return period(1L, 2030, 1, AcademicPeriodEntity.AcademicPeriodStatus.OPEN);
    }

    @Test
    void createOpensANewPeriod(){
        AcademicPeriodResponseDTO result = service.create(new AcademicPeriodRequestDTO(2030, 1));

        assertThat(result.academicYear()).isEqualTo(2030);
        assertThat(result.academicPeriodStatus()).isEqualTo(AcademicPeriodEntity.AcademicPeriodStatus.OPEN);
    }

    @Test
    void createRejectsMissingYear(){
        assertThatThrownBy(() -> service.create(new AcademicPeriodRequestDTO(null, 1)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("year");
    }

    @Test
    void createRejectsMissingSemester(){
        assertThatThrownBy(() -> service.create(new AcademicPeriodRequestDTO(2030, null)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("semester");
    }

    @Test
    void createRejectsSemesterOutOfRange(){
        assertThatThrownBy(() -> service.create(new AcademicPeriodRequestDTO(2030, 3)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("1 or 2");
    }

    @Test
    void createRejectsDuplicatedPeriod(){
        when(academicPeriodRepository.existsByAcademicYearAndAcademicSemester(2030, 1)).thenReturn(true);

        assertThatThrownBy(() -> service.create(new AcademicPeriodRequestDTO(2030, 1)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("already exists");
        verify(academicPeriodRepository, never()).save(any());
    }

    @Test
    void getAllReturnsEveryPeriod(){
        when(academicPeriodRepository.findAll()).thenReturn(List.of(openPeriod()));

        assertThat(service.getAll()).hasSize(1);
    }

    @Test
    void getByIdReturnsPeriod(){
        when(academicPeriodRepository.findById(1L)).thenReturn(Optional.of(openPeriod()));

        assertThat(service.getById(1L).academicSemester()).isEqualTo(1);
    }

    @Test
    void getByIdFailsWhenMissing(){
        when(academicPeriodRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(9L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateChangesFreePeriod(){
        when(academicPeriodRepository.findById(1L)).thenReturn(Optional.of(openPeriod()));

        AcademicPeriodResponseDTO result = service.update(1L, new AcademicPeriodRequestDTO(2031, 2));

        assertThat(result.academicYear()).isEqualTo(2031);
        assertThat(result.academicSemester()).isEqualTo(2);
    }

    @Test
    void updateKeepingSamePeriodDoesNotCheckDuplicates(){
        when(academicPeriodRepository.findById(1L)).thenReturn(Optional.of(openPeriod()));
        when(academicPeriodRepository.existsByAcademicYearAndAcademicSemester(2030, 1)).thenReturn(true);

        assertThat(service.update(1L, new AcademicPeriodRequestDTO(2030, 1)).academicYear()).isEqualTo(2030);
    }

    @Test
    void updateRejectsClosedPeriod(){
        when(academicPeriodRepository.findById(1L))
                .thenReturn(Optional.of(period(1L, 2030, 1, AcademicPeriodEntity.AcademicPeriodStatus.CLOSED)));

        assertThatThrownBy(() -> service.update(1L, new AcademicPeriodRequestDTO(2031, 1)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("closed");
    }

    @Test
    void updateRejectsPeriodWithSections(){
        AcademicPeriodEntity open = openPeriod();
        when(academicPeriodRepository.findById(1L)).thenReturn(Optional.of(open));
        when(sectionRepository.existsByAcademicPeriod(open)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, new AcademicPeriodRequestDTO(2031, 1)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("associated");
    }

    @Test
    void updateRejectsPeriodWithTuitions(){
        AcademicPeriodEntity open = openPeriod();
        when(academicPeriodRepository.findById(1L)).thenReturn(Optional.of(open));
        when(tuitionRepository.existsByAcademicPeriod(open)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, new AcademicPeriodRequestDTO(2031, 1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void updateRejectsPeriodWithEnrollments(){
        AcademicPeriodEntity open = openPeriod();
        when(academicPeriodRepository.findById(1L)).thenReturn(Optional.of(open));
        when(enrollmentRepository.existsByAcademicPeriod(open)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, new AcademicPeriodRequestDTO(2031, 1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void updateRejectsAPeriodThatAlreadyExists(){
        when(academicPeriodRepository.findById(1L)).thenReturn(Optional.of(openPeriod()));
        when(academicPeriodRepository.existsByAcademicYearAndAcademicSemester(2031, 2)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, new AcademicPeriodRequestDTO(2031, 2)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("already exists");
    }

    @Test
    void deleteRemovesFreePeriod(){
        AcademicPeriodEntity open = openPeriod();
        when(academicPeriodRepository.findById(1L)).thenReturn(Optional.of(open));

        service.delete(1L);

        verify(academicPeriodRepository).delete(open);
    }

    @Test
    void deleteRejectsPeriodWithAssociations(){
        AcademicPeriodEntity open = openPeriod();
        when(academicPeriodRepository.findById(1L)).thenReturn(Optional.of(open));
        when(sectionRepository.existsByAcademicPeriod(open)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(IllegalStateException.class);
        verify(academicPeriodRepository, never()).delete(any());
    }
}
