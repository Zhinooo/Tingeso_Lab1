package SIGA_IPT.backend.repositories;

import SIGA_IPT.backend.entities.CareerEntity;
import SIGA_IPT.backend.entities.StudyplanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyplanRepository extends JpaRepository<StudyplanEntity, Long>{

    boolean existsByCareer(CareerEntity career);

    boolean existsByStudyplanIdentifierAndCareer(String studyplanIdentifier, CareerEntity career);

    Optional<StudyplanEntity> findByCareerAndStudyplanStatus(CareerEntity career, StudyplanEntity.StudyplanStatus studyplanStatus);

    Optional<StudyplanEntity> findByStudyplanIdentifierAndCareer(String studyplanIdentifier, CareerEntity career);

}
