package org.ooad.server.controller;

import org.ooad.server.command.MedicationCommand;
import org.ooad.server.command.SnoozeCommand;
import org.ooad.server.command.TakeCommand;
import org.ooad.server.model.BaseMedication;
import org.ooad.server.service.MedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller.
 * Acts as the "Client" for the Command objects and the entry point for the UI.
 */
@RestController
@RequestMapping("/api/medications")
@CrossOrigin(origins = "*") // Allow frontend access
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
                                        @RequestParam(defaultValue = "false") boolean foodSensitive) {
        return medicationService.addMedication(name, dosageForm, foodSensitive);
    }

    // COMMAND PATTERN ENDPOINTS

    @PostMapping("/{id}/take")
    public String takeMedication(@PathVariable Long id) {
        // Create the command
        MedicationCommand takeCommand = new TakeCommand(medicationService, id);
        // Execute the command
        takeCommand.execute();
        return "Taken successfully";
    }

    @PostMapping("/{id}/snooze")
    public String snoozeMedication(@PathVariable Long id) {
        // Create the command
        MedicationCommand snoozeCommand = new SnoozeCommand(medicationService, id);
        // Execute the command
        snoozeCommand.execute();
        return "Snoozed successfully";
    }
}
