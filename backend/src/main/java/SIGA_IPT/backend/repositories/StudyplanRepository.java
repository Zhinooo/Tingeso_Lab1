package SIGA_IPT.backend.repositories;

import SIGA_IPT.backend.entities.StudyplanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyplanRepository extends JpaRepository<StudyplanEntity, Long>{
}
