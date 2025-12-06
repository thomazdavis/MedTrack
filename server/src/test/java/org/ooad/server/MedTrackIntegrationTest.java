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
@Transactional // Rolls back DB changes after each test
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
        Medication med1 = new BaseMedication("Warfarin", "Pill");
        List<Medication> existingMeds = List.of(med1);

        Medication newMed = new BaseMedication("Aspirin", "Tablet");

        String warning = interactionStrategy.checkInteraction(newMed, existingMeds);

        assertNotNull(warning);
        assertTrue(warning.contains("CRITICAL INTERACTION"));
    }

    @Test
    void testCommandPatternTakeAction() {
        BaseMedication med = medicationService.addMedication("Vitamin C", "Tablet", false);
        LocalDateTime originalTime = med.getNextDueTime();
        Long id = med.getId();

        TakeCommand command = new TakeCommand(medicationService, id);
        command.execute();

        BaseMedication updatedMed = medicationRepository.findById(id).orElseThrow();
        assertTrue(updatedMed.getNextDueTime().isAfter(originalTime));
    }

    @Test
    void testCommandPatternSnoozeAction() {
        BaseMedication med = medicationService.addMedication("Allergy Med", "Pill", false);
        LocalDateTime originalTime = med.getNextDueTime();
        Long id = med.getId();

        SnoozeCommand command = new SnoozeCommand(medicationService, id);
        command.execute();

        BaseMedication updatedMed = medicationRepository.findById(id).orElseThrow();
        assertTrue(updatedMed.getNextDueTime().isAfter(originalTime));
    }

    @Test
    void testDeleteMedication() {
        BaseMedication med = medicationService.addMedication("To Delete", "Pill", false);
        Long id = med.getId();

        medicationService.deleteMedication(id);

        Optional<BaseMedication> deletedMed = medicationRepository.findById(id);
        assertTrue(deletedMed.isEmpty());
    }

    @Test
    void testUpdateMedication() {
        BaseMedication med = medicationService.addMedication("Original Name", "Pill", false);
        Long id = med.getId();

        medicationService.updateMedication(id, "Updated Name", "Syrup");

        BaseMedication updatedMed = medicationRepository.findById(id).orElseThrow();
        assertEquals("Updated Name", updatedMed.getName());
        assertEquals("Syrup", updatedMed.getDosageForm());
    }

    @Test
    void testUserRegistrationAndLogin() {
        String username = "testuser";
        String password = "password123";
        User user = userService.registerNewUser(username, password);

        assertNotNull(user.getId());
        assertEquals(username, user.getUsername());
        assertNotEquals(password, user.getPassword()); // Password should be hashed

        boolean isValid = userService.validateUser(username, password);
        assertTrue(isValid);

        boolean isInvalid = userService.validateUser(username, "wrongpassword");
        assertFalse(isInvalid);
    }
}