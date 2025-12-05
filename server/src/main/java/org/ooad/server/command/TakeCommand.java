package org.ooad.server.command;

import org.ooad.server.service.MedicationService;

/**
 * To "Take" a medication.
 * It decouples the requester (Controller/UI) from the receiver (Service).
 */
public class TakeCommand implements MedicationCommand {

    private final MedicationService medicationService;
    private final Long medicationId;

    public TakeCommand(MedicationService medicationService, Long medicationId) {
        this.medicationService = medicationService;
        this.medicationId = medicationId;
    }

    @Override
    public void execute() {
        medicationService.takeMedication(medicationId);
    }
}