package org.ooad.server.controller;

import org.ooad.server.command.MedicationCommand;
import org.ooad.server.command.SnoozeCommand;
import org.ooad.server.command.TakeCommand;
import org.ooad.server.model.BaseMedication;
import org.ooad.server.service.MedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller.
 * Handles Medication CRUD and Command (Take/Snooze) execution.
 */
@RestController
@RequestMapping("/api/medications")
@CrossOrigin(origins = "*")
public class MedicationController {

    private final MedicationService medicationService;

    @Autowired
    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @GetMapping
    public List<BaseMedication> getAllMedications() {
        return medicationService.getAllMedications();
    }

    @PostMapping
    public BaseMedication addMedication(@RequestParam String name,
                                        @RequestParam String dosageForm,
                                        @RequestParam(defaultValue = "false") boolean foodSensitive,
                                        @RequestParam(defaultValue = "1") int dosagesPerDay) {
        return medicationService.addMedication(name, dosageForm, foodSensitive, dosagesPerDay);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseMedication> updateMedication(@PathVariable Long id,
                                                           @RequestParam String name,
                                                           @RequestParam String dosageForm,
                                                           @RequestParam(defaultValue = "1") int dosagesPerDay) {
        try {
            BaseMedication updatedMed = medicationService.updateMedication(id, name, dosageForm, dosagesPerDay);
            return ResponseEntity.ok(updatedMed);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedication(@PathVariable Long id) {
        try {
            medicationService.deleteMedication(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/take")
    public String takeMedication(@PathVariable Long id) {
        System.out.println("Received Take Command for ID: " + id);
        new TakeCommand(medicationService, id).execute();
        return "Taken successfully";
    }

    @PostMapping("/{id}/snooze")
    public String snoozeMedication(@PathVariable Long id) {
        System.out.println("Received Snooze Command for ID: " + id);
        new SnoozeCommand(medicationService, id).execute();
        return "Snoozed successfully";
    }
}