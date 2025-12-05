package org.ooad.server.command;

import org.ooad.server.service.MedicationService;

/**
 * To "Snooze" a medication.
 */
public class SnoozeCommand implements MedicationCommand {

    private final MedicationService medicationService;
    private final Long medicationId;

    public SnoozeCommand(MedicationService medicationService, Long medicationId) {
        this.medicationService = medicationService;
        this.medicationId = medicationId;
    }

    @Override
    public void execute() {
        medicationService.snoozeMedication(medicationId);
    }
}