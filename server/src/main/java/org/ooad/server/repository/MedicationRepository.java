package org.ooad.server.repository;

import org.ooad.server.model.BaseMedication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA Repository Abstraction for Medication persistence.
 * This is part of the overall strategy to "Code to Abstractions" (Repository pattern)
 * and uses Spring's Dependency Injection system.
 */
@Repository
public interface MedicationRepository extends JpaRepository<BaseMedication, Long> {
    // Spring Data JPA automatically provides basic CRUD methods (findAll, save, findById, etc.)
    // We can add custom query methods here if needed, but for now, the basic interface is enough.
}