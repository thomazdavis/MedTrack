package org.ooad.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ooad.server.command.SnoozeCommand;
import org.ooad.server.command.TakeCommand;
import org.ooad.server.factory.MedicationFactory;
import org.ooad.server.model.BaseMedication;
import org.ooad.server.model.Medication;
import org.ooad.server.model.User;
import org.ooad.server.repository.MedicationRepository;
import org.ooad.server.repository.UserRepository;
import org.ooad.server.service.MedicationService;
import org.ooad.server.service.UserService;
import org.ooad.server.strategy.InteractionStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class MedTrackIntegrationTest {

    @Autowired
    private MedicationService medicationService;

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private MedicationFactory medicationFactory;

    @Autowired
    private InteractionStrategy interactionStrategy;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        medicationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testFactoryAndDecoratorPattern() {
        Medication med1 = medicationFactory.createMedication("Aspirin", "Tablet", false);
        assertEquals("Standard", med1.getAttributes());

        Medication med2 = medicationFactory.createMedication("Antibiotic", "Capsule", true);
        assertTrue(med2.getAttributes().contains("Food Sensitive"));
    }

    @Test
    void testStrategyPatternInteractionCheck() {
        Medication med1 = new BaseMedication("Warfarin", "Pill", 1);
        List<Medication> existingMeds = List.of(med1);

        Medication newMed = new BaseMedication("Aspirin", "Tablet", 1);

        String warning = interactionStrategy.checkInteraction(newMed, existingMeds);

        assertNotNull(warning);
        assertTrue(warning.contains("CRITICAL INTERACTION"));
    }

    @Test
    void testCommandPatternTakeAction() {
        // Use 4 arguments: name, form, foodSensitive, dosagesPerDay
        BaseMedication med = medicationService.addMedication("Vitamin C", "Tablet", false, 1);
        LocalDateTime originalTime = med.getNextDueTime();
        Long id = med.getId();

        TakeCommand command = new TakeCommand(medicationService, id);
        command.execute();

        BaseMedication updatedMed = medicationRepository.findById(id).orElseThrow();
        assertTrue(updatedMed.getNextDueTime().isAfter(originalTime));
    }

    @Test
    void testCommandPatternSnoozeAction() {
        // Use 4 arguments
        BaseMedication med = medicationService.addMedication("Allergy Med", "Pill", false, 1);
        LocalDateTime originalTime = med.getNextDueTime();
        Long id = med.getId();

        SnoozeCommand command = new SnoozeCommand(medicationService, id);
        command.execute();

        BaseMedication updatedMed = medicationRepository.findById(id).orElseThrow();
        assertTrue(updatedMed.getNextDueTime().isAfter(originalTime));
    }

    @Test
    void testDeleteMedication() {
        BaseMedication med = medicationService.addMedication("To Delete", "Pill", false, 1);
        Long id = med.getId();

        medicationService.deleteMedication(id);

        Optional<BaseMedication> deletedMed = medicationRepository.findById(id);
        assertTrue(deletedMed.isEmpty());
    }

    @Test
    void testUpdateMedication() {
        BaseMedication med = medicationService.addMedication("Original Name", "Pill", false, 1);
        Long id = med.getId();

        // Use 4 arguments for update: id, name, form, dosagesPerDay
        medicationService.updateMedication(id, "Updated Name", "Syrup", 2);

        BaseMedication updatedMed = medicationRepository.findById(id).orElseThrow();
        assertEquals("Updated Name", updatedMed.getName());
        assertEquals("Syrup", updatedMed.getDosageForm());
        assertEquals(2, updatedMed.getDosagesPerDay());
    }

    @Test
    void testUserRegistrationAndLogin() {
        String username = "testuser";
        String password = "password123";
        User user = userService.registerNewUser(username, password);

        assertNotNull(user.getId());
        assertEquals(username, user.getUsername());
        assertNotEquals(password, user.getPassword());

        boolean isValid = userService.validateUser(username, password);
        assertTrue(isValid);

        boolean isInvalid = userService.validateUser(username, "wrongpassword");
        assertFalse(isInvalid);
    }
}