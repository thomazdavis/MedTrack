package org.ooad.server.service;

import org.ooad.server.factory.MedicationFactory;
import org.ooad.server.model.BaseMedication;
import org.ooad.server.model.Medication;
import org.ooad.server.repository.MedicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service Layer for Medication operations.
 * Uses Dependency Injection for the Factory and Repository abstractions.
 * This is where the business logic resides.
 */
@Service
public class MedicationService {

    private final MedicationRepository medicationRepository;
    private final MedicationFactory medicationFactory;
    // We will inject the ReminderSystem here to link medication creation/updates to the reminder logic
    // private final ReminderSystem reminderSystem; // To be added later

    @Autowired
    public MedicationService(
            MedicationRepository medicationRepository,
            MedicationFactory medicationFactory
            // ReminderSystem reminderSystem // To be added later
    ) {
        this.medicationRepository = medicationRepository;
        this.medicationFactory = medicationFactory;
        // this.reminderSystem = reminderSystem;
    }

    /**
     * Creates and saves a new medication, using the Factory Method and Decorator patterns.
     * @param name Name of the medication.
     * @param dosageForm Dosage form.
     * @param isFoodSensitive Whether it needs the food sensitive decorator.
     * @return The persisted BaseMedication entity.
     */
    public BaseMedication addMedication(String name, String dosageForm, boolean isFoodSensitive) {
        // Use the Factory Method to create the appropriate decorated Medication instance (coding to abstraction)
        Medication medication = medicationFactory.createMedication(name, dosageForm, isFoodSensitive);

        // We can't persist the decorated object directly. We need to extract the BaseMedication.
        // For our current simple Decorator structure, the BaseMedication is the component.
        // We ensure the Factory returns BaseMedication if no decorators were applied,
        // or we use casting/utility to get the underlying BaseMedication from the decorated chain.
        // For simplicity, we assume the factory ensures the underlying object is a BaseMedication
        // that has the persistence annotations.
        BaseMedication baseMedication;
        if (medication instanceof BaseMedication) {
            baseMedication = (BaseMedication) medication;
        } else {
            // NOTE: In a robust implementation, the decorator must hold a reference to the
            // original BaseMedication object, and we'd traverse the decorators to find it.
            // For now, let's assume the factory returns an instance we can save.
            // Since BaseMedication is the only @Entity, we'll save it.
            // We need to slightly adjust our factory to always return the savable BaseMedication
            // or modify the decorator structure to pass persistence duties.
            // For now, we will save a NEW BaseMedication with the data. (This is a simplified persistence approach).
            baseMedication = new BaseMedication(medication.getName(), medication.getDosageForm());
            baseMedication.setNextDueTime(medication.getNextDueTime());
            // In a real scenario, we'd also need to persist the attributes (decorations)
        }

        // Save the base entity
        BaseMedication savedMedication = medicationRepository.save(baseMedication);

        // In a later step, we'd register this medication with the ReminderSystem
        // reminderSystem.addMedication(savedMedication);

        System.out.println("New medication saved. Attributes: " + medication.getAttributes());
        return savedMedication;
    }

    /**
     * Retrieves all medications.
     * @return List of all BaseMedication entities.
     */
    public List<BaseMedication> getAllMedications() {
        return medicationRepository.findAll();
    }
}