package SIGA_IPT.backend.services;

import SIGA_IPT.backend.dtos.CareerDTOs.CareerRequestDTO;
import SIGA_IPT.backend.dtos.CareerDTOs.CareerResponseDTO;
import SIGA_IPT.backend.entities.CareerEntity;
import SIGA_IPT.backend.mappers.CareerMapper;
import SIGA_IPT.backend.repositories.CareerRepository;
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
class CareerServiceTest {

    @Mock
    private CareerRepository careerRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private StudyplanRepository studyplanRepository;

    private CareerService careerService;

    @BeforeEach
    void setUp(){
        careerService = new CareerService(careerRepository, studentRepository, studyplanRepository, new CareerMapper());
        when(careerRepository.save(any(CareerEntity.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private CareerEntity career(Long id, String code){
        CareerEntity career = new CareerEntity();
        career.setCareerId(id);
        career.setCareerCode(code);
        career.setCareerName("Name " + code);
        career.setCareerDescription("Description");
        career.setCareerStatus(CareerEntity.CareerStatus.ACTIVE);
        return career;
    }

    @Test
    void createSavesActiveCareer(){
        when(careerRepository.existsByCareerCode("10450")).thenReturn(false);

        CareerResponseDTO result = careerService.create(new CareerRequestDTO("10450", "Programmer", "Desc"));

        assertThat(result.careerCode()).isEqualTo("10450");
        assertThat(result.careerStatus()).isEqualTo(CareerEntity.CareerStatus.ACTIVE);
    }

    @Test
    void createRejectsBlankCode(){
        assertThatThrownBy(() -> careerService.create(new CareerRequestDTO(" ", "N", "D")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("code");
        verify(careerRepository, never()).save(any());
    }

    @Test
    void createRejectsNullName(){
        assertThatThrownBy(() -> careerService.create(new CareerRequestDTO("C", null, "D")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("name");
    }

    @Test
    void createRejectsBlankDescription(){
        assertThatThrownBy(() -> careerService.create(new CareerRequestDTO("C", "N", "")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("description");
    }

    @Test
    void createRejectsDuplicatedCode(){
        when(careerRepository.existsByCareerCode("C")).thenReturn(true);

        assertThatThrownBy(() -> careerService.create(new CareerRequestDTO("C", "N", "D")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("already exists");
    }

    @Test
    void getAllMapsEveryCareer(){
        when(careerRepository.findAll()).thenReturn(List.of(career(1L, "A"), career(2L, "B")));

        assertThat(careerService.getAll()).extracting(CareerResponseDTO::careerCode).containsExactly("A", "B");
    }

    @Test
    void getByIdReturnsCareer(){
        when(careerRepository.findById(1L)).thenReturn(Optional.of(career(1L, "A")));

        assertThat(careerService.getById(1L).careerId()).isEqualTo(1L);
    }

    @Test
    void getByIdFailsWhenMissing(){
        when(careerRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> careerService.getById(9L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateChangesFieldsWhenCareerIsFree(){
        CareerEntity existing = career(1L, "A");
        when(careerRepository.findById(1L)).thenReturn(Optional.of(existing));

        CareerResponseDTO result = careerService.update(1L, new CareerRequestDTO("A", "New name", "New desc"));

        assertThat(result.careerName()).isEqualTo("New name");
    }

    @Test
    void updateAllowsChangingToANewCode(){
        when(careerRepository.findById(1L)).thenReturn(Optional.of(career(1L, "A")));
        when(careerRepository.existsByCareerCode("B")).thenReturn(false);

        assertThat(careerService.update(1L, new CareerRequestDTO("B", "N", "D")).careerCode()).isEqualTo("B");
    }

    @Test
    void updateRejectsWhenCareerHasStudents(){
        CareerEntity existing = career(1L, "A");
        when(careerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(studentRepository.existsByCareer(existing)).thenReturn(true);

        assertThatThrownBy(() -> careerService.update(1L, new CareerRequestDTO("A", "N", "D")))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("students");
    }

    @Test
    void updateRejectsWhenCareerHasStudyPlans(){
        CareerEntity existing = career(1L, "A");
        when(careerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(studyplanRepository.existsByCareer(existing)).thenReturn(true);

        assertThatThrownBy(() -> careerService.update(1L, new CareerRequestDTO("A", "N", "D")))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("study plans");
    }

    @Test
    void updateRejectsCodeUsedByAnotherCareer(){
        when(careerRepository.findById(1L)).thenReturn(Optional.of(career(1L, "A")));
        when(careerRepository.existsByCareerCode("B")).thenReturn(true);

        assertThatThrownBy(() -> careerService.update(1L, new CareerRequestDTO("B", "N", "D")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("already exists");
    }

    @Test
    void changeStatusUpdatesCareer(){
        when(careerRepository.findById(1L)).thenReturn(Optional.of(career(1L, "A")));

        CareerResponseDTO result = careerService.changeStatus(1L, CareerEntity.CareerStatus.INACTIVE);

        assertThat(result.careerStatus()).isEqualTo(CareerEntity.CareerStatus.INACTIVE);
    }

    @Test
    void changeStatusRejectsNull(){
        assertThatThrownBy(() -> careerService.changeStatus(1L, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deleteRemovesFreeCareer(){
        CareerEntity existing = career(1L, "A");
        when(careerRepository.findById(1L)).thenReturn(Optional.of(existing));

        careerService.delete(1L);

        verify(careerRepository).delete(existing);
    }

    @Test
    void deleteRejectsCareerWithStudents(){
        CareerEntity existing = career(1L, "A");
        when(careerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(studentRepository.existsByCareer(existing)).thenReturn(true);

        assertThatThrownBy(() -> careerService.delete(1L)).isInstanceOf(IllegalStateException.class);
        verify(careerRepository, never()).delete(any());
    }

    @Test
    void deleteRejectsCareerWithStudyPlans(){
        CareerEntity existing = career(1L, "A");
        when(careerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(studyplanRepository.existsByCareer(existing)).thenReturn(true);

        assertThatThrownBy(() -> careerService.delete(1L)).isInstanceOf(IllegalStateException.class);
    }
}
