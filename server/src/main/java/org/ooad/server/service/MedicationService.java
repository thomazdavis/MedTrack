package org.ooad.server.service;

import org.ooad.server.factory.MedicationFactory;
import org.ooad.server.model.BaseMedication;
import org.ooad.server.model.Medication;
import org.ooad.server.repository.MedicationRepository;
import org.ooad.server.strategy.InteractionStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MedicationService {

    private final MedicationRepository medicationRepository;
    private final MedicationFactory medicationFactory;
    private final InteractionStrategy interactionStrategy;

    @Autowired
    public MedicationService(MedicationRepository medicationRepository,
                             MedicationFactory medicationFactory,
                             InteractionStrategy interactionStrategy) {
        this.medicationRepository = medicationRepository;
        this.medicationFactory = medicationFactory;
        this.interactionStrategy = interactionStrategy;
    }

    public BaseMedication addMedication(String name, String dosageForm, boolean isFoodSensitive) {
        // Create decorated medication
        Medication medication = medicationFactory.createMedication(name, dosageForm, isFoodSensitive);

        // Check for interactions (Strategy Pattern)
        List<BaseMedication> existingEntities = medicationRepository.findAll();
        // Convert entities to Medication interface list for the strategy
        List<Medication> existingMeds = existingEntities.stream()
                .map(m -> (Medication) m)
                .collect(Collectors.toList());

        String warning = interactionStrategy.checkInteraction(medication, existingMeds);
        if (warning != null) {
            System.err.println("WARNING: " + warning);
        }

        // Persist
        // Extract data from the decorated object to save the entity
        BaseMedication baseMedication;
        if (medication instanceof BaseMedication) {
            baseMedication = (BaseMedication) medication;
        } else {
            baseMedication = new BaseMedication(medication.getName(), medication.getDosageForm());
            baseMedication.setNextDueTime(medication.getNextDueTime());
        }

        BaseMedication saved = medicationRepository.save(baseMedication);
        System.out.println("Saved medication: " + saved.getName() + " [" + medication.getAttributes() + "]");
        return saved;
    }

    public List<BaseMedication> getAllMedications() {
        return medicationRepository.findAll();
    }

    public void takeMedication(Long id) {
        Optional<BaseMedication> medOpt = medicationRepository.findById(id);
        if (medOpt.isPresent()) {
            BaseMedication med = medOpt.get();
            System.out.println("Taking medication: " + med.getName());
            med.setNextDueTime(LocalDateTime.now().plusHours(24)); // Advance 24h
            medicationRepository.save(med);
        }
    }

    public void snoozeMedication(Long id) {
        Optional<BaseMedication> medOpt = medicationRepository.findById(id);
        if (medOpt.isPresent()) {
            BaseMedication med = medOpt.get();
            System.out.println("Snoozing medication: " + med.getName());
            med.setNextDueTime(LocalDateTime.now().plusMinutes(15)); // Advance 15m
            medicationRepository.save(med);
        }
    }
}