package org.ooad.server.repository;

import org.ooad.server.model.BaseMedication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MedicationRepository extends JpaRepository<BaseMedication, Long> {
    // Finds medications belonging to a specific user ID
    List<BaseMedication> findByUserId(Long userId);
}