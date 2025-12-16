package org.ooad.server.service;

import org.ooad.server.factory.MedicationFactory;
import org.ooad.server.model.BaseMedication;
import org.ooad.server.model.Medication;
import org.ooad.server.model.User;
import org.ooad.server.repository.MedicationRepository;
import org.ooad.server.repository.UserRepository;
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
    private final UserRepository userRepository;

    @Autowired
    public MedicationService(MedicationRepository medicationRepository,
                             MedicationFactory medicationFactory,
                             InteractionStrategy interactionStrategy,
                             UserRepository userRepository) {
        this.medicationRepository = medicationRepository;
        this.medicationFactory = medicationFactory;
        this.interactionStrategy = interactionStrategy;
        this.userRepository = userRepository;
    }

    public BaseMedication addMedication(String name, String dosageForm, boolean isFoodSensitive,
                                        int dosagesPerDay, String username, LocalDateTime startTime) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        Medication medication = medicationFactory.createMedication(name, dosageForm, isFoodSensitive);

        List<BaseMedication> existingEntities = medicationRepository.findByUserId(user.getId());
        List<Medication> existingMeds = existingEntities.stream()
                .map(m -> (Medication) m)
                .collect(Collectors.toList());

        String warning = interactionStrategy.checkInteraction(medication, existingMeds);
        if (warning != null) {
            System.err.println("WARNING: " + warning);
        }

        BaseMedication baseMedication;
        if (medication instanceof BaseMedication) {
            baseMedication = (BaseMedication) medication;
        } else {
            baseMedication = new BaseMedication(medication.getName(), medication.getDosageForm(), dosagesPerDay);
            baseMedication.setNextDueTime(medication.getNextDueTime());
            baseMedication.setAttributes(medication.getAttributes());
        }

        baseMedication.setDosagesPerDay(dosagesPerDay);
        baseMedication.setUserId(user.getId());

        // FIX: Override start time if custom time provided
        if (startTime != null) {
            baseMedication.setNextDueTime(startTime);
        }

        BaseMedication saved = medicationRepository.save(baseMedication);
        System.out.println("Saved for user " + username + ": " + saved.getName());
        return saved;
    }

    public List<BaseMedication> getUserMedications(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return medicationRepository.findByUserId(user.getId());
    }

    public BaseMedication updateMedication(Long id, String name, String dosageForm, int dosagesPerDay) {
        Optional<BaseMedication> medOpt = medicationRepository.findById(id);
        if (medOpt.isPresent()) {
            BaseMedication med = medOpt.get();
            med.setName(name);
            med.setDosageForm(dosageForm);
            med.setDosagesPerDay(dosagesPerDay);
            medicationRepository.save(med);
            return med;
        }
        throw new IllegalArgumentException("Medication ID " + id + " not found.");
    }

    public void deleteMedication(Long id) {
        if (medicationRepository.existsById(id)) {
            medicationRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Medication ID " + id + " not found.");
        }
    }

    public void takeMedication(Long id) {
        Optional<BaseMedication> medOpt = medicationRepository.findById(id);
        if (medOpt.isPresent()) {
            BaseMedication med = medOpt.get();
            int hoursInterval = 24 / Math.max(1, med.getDosagesPerDay());
            med.setNextDueTime(LocalDateTime.now().plusHours(hoursInterval));
            medicationRepository.save(med);
        }
    }

    public void snoozeMedication(Long id) {
        Optional<BaseMedication> medOpt = medicationRepository.findById(id);
        if (medOpt.isPresent()) {
            BaseMedication med = medOpt.get();
            med.setNextDueTime(LocalDateTime.now().plusMinutes(15));
            medicationRepository.save(med);
        }
    }
}