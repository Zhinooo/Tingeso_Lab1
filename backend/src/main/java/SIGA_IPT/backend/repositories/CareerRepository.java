package SIGA_IPT.backend.repositories;

import SIGA_IPT.backend.entities.CareerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CareerRepository extends JpaRepository<CareerEntity, Long>{

    boolean existsByCareerCode(String careerCode);

    Optional<CareerEntity> findByCareerCode(String careerCode);

}
