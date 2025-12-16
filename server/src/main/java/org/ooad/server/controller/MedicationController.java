package org.ooad.server.controller;

import org.ooad.server.command.SnoozeCommand;
import org.ooad.server.command.TakeCommand;
import org.ooad.server.model.BaseMedication;
import org.ooad.server.service.MedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

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
    public List<BaseMedication> getMedications(@RequestParam String username) {
        return medicationService.getUserMedications(username);
    }

    @PostMapping
    public BaseMedication addMedication(@RequestParam String name,
                                        @RequestParam String dosageForm,
                                        @RequestParam(defaultValue = "false") boolean foodSensitive,
                                        @RequestParam(defaultValue = "1") int dosagesPerDay,
                                        @RequestParam String username,
                                        // Optional Start Time (ISO Date Time format)
                                        @RequestParam(required = false)
                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime) {

        return medicationService.addMedication(name, dosageForm, foodSensitive, dosagesPerDay, username, startTime);
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
        new TakeCommand(medicationService, id).execute();
        return "Taken successfully";
    }

    @PostMapping("/{id}/snooze")
    public String snoozeMedication(@PathVariable Long id) {
        new SnoozeCommand(medicationService, id).execute();
        return "Snoozed successfully";
    }
}